package com.androidlogsuite.service;

import com.androidlogsuite.model.prebuild.BatteryStats;
import com.androidlogsuite.model.prebuild.DiskStats;
import com.androidlogsuite.model.prebuild.MemInfo;
import com.androidlogsuite.model.prebuild.UsageStats;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading blocks from file, of which content is the result of command `adb bugreport` or `adb logcat`.
 * <p>
 * You could retrieve the following information by typing 'adb bugreport' from the console:
 * <li>diskstats, keyword is 'DUMP OF SERVICE diskstats'</li>
 * <li>batterystats, keyword is 'CHECKIN BATTERYSTATS (dumpsys batterystats -c)'</li>
 * <li>netstats, keyword is 'CHECKIN NETSTATS (dumpsys netstats --checkin)'</li>
 * <li>procstats, keyword is 'CHECKIN PROCSTATS (dumpsys procstats -c)'</li>
 * <li>usagestats, keyword is 'CHECKIN USAGESTATS (dumpsys usagestats -c)'</li>
 * <li>meminfo, keyword is 'CHECKIN MEMINFO (dumpsys meminfo --checkin)'</li>
 * </p>
 *
 * @author duanqizhi
 */
public class FileReadWorker implements Runnable {

    private static final String TAG = "FileReadWorker";

    private static final long WAIT_SECONDS = 30 * 1000;

    private static Map<String, String> sKeywords = new HashMap<String, String>(4);

    private static String BLOCK_END = "dumpsys: [\\d\\.]+s elapsed";

    static {
        sKeywords.put(DiskStats.ADB_COMMAND, "DUMP OF SERVICE diskstats(.*?)------------");
        sKeywords.put(BatteryStats.ADB_COMMAND, "CHECKIN BATTERYSTATS(.*?)" + BLOCK_END);
        sKeywords.put(MemInfo.ADB_COMMAND, "CHECKIN MEMINFO(.*?)" + BLOCK_END);
        sKeywords.put(UsageStats.ADB_COMMAND, "CHECKIN USAGESTATS(.*?)" + BLOCK_END);
    }

    String mFileName;
    private CharSequence mFileContent;
    private volatile boolean bContentRetrieved;

    private List<Connection> mConnections;
    private WorkFinishedListener mFinishedListener;

    private volatile boolean bInService;

    static class Connection {
        String mAdbCommand;
        SocketChannel mSocketChannel;

        Connection(String adbCommand, SocketChannel socketChannel) {
            mAdbCommand = adbCommand;
            mSocketChannel = socketChannel;
        }
    }

    interface WorkFinishedListener {
        void onReadFinished(FileReadWorker fileReadWorker);
    }

    FileReadWorker(String filename) {
        mFileName = filename;
        mConnections = new ArrayList<Connection>();
        bContentRetrieved = false;
    }

    void setWorkFinishedListener(WorkFinishedListener finishedListener) {
        this.mFinishedListener = finishedListener;
    }

    void addConnection(Connection connection) {
        synchronized (mConnections) {
            mConnections.add(connection);
            mConnections.notify();
        }
    }

    void start() {
        bInService = true;
        ThreadsPool.getThreadsPool().addTask(this);
    }

    void stop() {
        bInService = false;
        synchronized (mConnections) {
            mConnections.notifyAll();
        }
    }

    @Override
    public void run() {
        while (bInService) {
            Connection c;
            synchronized (mConnections) {
                if (mConnections.size() == 0) {
                    try {
                        mConnections.wait(WAIT_SECONDS);

                    } catch (InterruptedException e) {
                        Log.d(TAG, this + " has been interrupted.");
                        e.printStackTrace();
                    }
                }

                /* Here, wake up to work */

                if (mConnections.size() > 0) {
                    c = mConnections.remove(0);
                } else {
                    Log.d(TAG, "Wake up, but no work to do. Sorry, I will kill myself.");
                    notifyWorkFinished();
                    break;
                }
            }

            processConnection(c);
        }

        stop();
    }

    private void processConnection(Connection c) {
        if (c == null) {
            return;
        }

        try {
            Log.d(TAG, "Processing " + c.mAdbCommand);

            readFileContent();
            String content = searchBlock(c.mAdbCommand);
            c.mSocketChannel.write(ByteBuffer.wrap(content.getBytes()));

            closeConnection(c);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(Connection c) throws  IOException{
        c.mSocketChannel.close();
    }

    /**
     * File content might be huge. We should carefully open file as "Memory Mapped File"
     * It is time costly when first time invoked.
     * @throws IOException
     */
    private void readFileContent () throws IOException {
        if (bContentRetrieved) {
            return;
        }

        final String filename = adjustFilename();
        final FileInputStream input = new FileInputStream(filename);
        final FileChannel channel = input.getChannel();

        // Create a read-only CharBuffer on the file
        final ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0,
                (int)channel.size() /* TODO adjust the map size */);

        // Time-costly on large file
        mFileContent = Charset.forName("8859_1").newDecoder().decode(byteBuffer);
        bContentRetrieved = true;

        channel.close();
        input.close();
    }

    private String adjustFilename() {
        String filename = System.getProperty("user.dir") + File.separator + mFileName;
        return filename;
    }

    private void closeFileReader() {
    }

    private String searchBlock(String cmd) {
        String keyword = sKeywords.get(cmd);

        // Create matcher on file
        Pattern pattern = Pattern.compile(keyword, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(mFileContent);

        StringBuilder content = new StringBuilder();

        // Find all matches
        while (matcher.find()) {
            // Get the matching string
            String match = matcher.group();
            content.append(match);
        }

        Log.d(TAG, content);
        return content.toString();
    }

    private void notifyWorkFinished() {
        if (mFinishedListener != null) {
            mFinishedListener.onReadFinished(this);
        }
    }
}
