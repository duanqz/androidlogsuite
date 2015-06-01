package com.androidlogsuite.service;

import com.androidlogsuite.model.prebuild.BatteryStats;
import com.androidlogsuite.model.prebuild.DiskStats;
import com.androidlogsuite.model.prebuild.MemInfo;
import com.androidlogsuite.model.prebuild.UsageStats;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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
public class FileReadService implements Runnable {

    private static final String TAG = FileReadService.class.getSimpleName();

    private static Map<String, String> sKeywords = new HashMap<String, String>(4);

    private volatile boolean bServiceStoped;

    private ServerSocket mServerSocket;

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT  = 9527;

    static {
        sKeywords.put(DiskStats.ADB_COMMAND, "DUMP OF SERVICE diskstats");
        sKeywords.put(BatteryStats.ADB_COMMAND, "dumpsys batterystats -c");
        sKeywords.put(MemInfo.ADB_COMMAND, "dumpsys meminfo -c");
        sKeywords.put(UsageStats.ADB_COMMAND, "dumpsys usagestats -c");
    }

    public FileReadService() {
        try {
            mServerSocket = new ServerSocket(SERVER_PORT);

        } catch (IOException e) {
            Log.d(TAG, "Error when creating server socket. " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while(!bServiceStoped) {
            try {
                Socket socket = mServerSocket.accept();
                ThreadsPool.getThreadsPool().addTask(new WorkThread(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        try {
            bServiceStoped = true;
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class WorkThread implements Runnable {

        Socket mSocket;

        WorkThread(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            Log.d(TAG, "Client port is " + mSocket.getLocalPort());
        }
    }
}

