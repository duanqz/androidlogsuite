package com.androidlogsuite.model.prebuild;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.androidlogsuite.model.Model;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.Task;
import com.androidlogsuite.util.Log;

public class UsageStats extends Model {
    public static String ADB_COMMAND = "shell:dumpsys usagestats -c";
    public static String TAG = "UsageStats";

    private AdbTask mAdbClient;

    public Task getTask() {
        return mAdbClient;
    }

    @Override
    public void draw(Output output) {
        Log.d(TAG, "UsageStats draw");
        // output.drawObject(getProcessUsageChart());
    }

    public String getModelName() {
        return TAG;
    }

    private static class ProcessUsageItem {
        public ProcessUsageItem(String[] values) {
            mPkgName = values[0];
            mStartCount = Long.parseLong(values[1]);
            mUsageTime = Long.parseLong(values[2]);
        }

        String mPkgName;
        long mStartCount;
        long mUsageTime;
    }

    ArrayList<ProcessUsageItem> mProcessUsageItems;

    public UsageStats() {
        super();
        mModelParser = new UsageStatsParser(this);
        mAdbClient = new AdbTask(ADB_COMMAND, this);

    }

    /*
     * private JFreeChart getProcessUsageChart(){ return null; }
     */
    public void destroy() {
        super.destroy();
    }

    private void createModelData() {
        if (mProcessUsageItems != null)
            return;
        mProcessUsageItems = new ArrayList<ProcessUsageItem>();
    }

    static public class UsageStatsParser extends ModelParser {

        UsageStats mUsageStats;
        boolean mbParsingFinished;
        static String LINE_WITH_PROCESS = "P:";
        static Pattern mProcessPattern = Pattern.compile("(?:P\\:)(.+)",
                Pattern.CASE_INSENSITIVE);

        public UsageStatsParser(Model usageStats) {
            mUsageStats = (UsageStats) usageStats;
        }

        @Override
        public boolean parseWithoutParseConfiguration(ByteBuffer parsingBuffer,
                int offset) {
            if (parsingBuffer == null)
                return true;
            byte[] buffer = parsingBuffer.array();
            int length = parsingBuffer.position() - offset;

            mUsageStats.createModelData();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    buffer, offset, length);
            LineNumberReader lineNumberReader = new LineNumberReader(
                    new InputStreamReader(byteArrayInputStream));

            try {
                while (mbParsingFinished == false) {
                    String line = lineNumberReader.readLine();
                    if (line == null)
                        break;
                    if (line.startsWith(LINE_WITH_PROCESS)) {
                        Matcher matcher = mProcessPattern.matcher(line);
                        if (matcher.find()) {
                            String value = matcher.group(1);
                            String[] values = value.split(",");
                            ProcessUsageItem item = new ProcessUsageItem(values);
                            mUsageStats.mProcessUsageItems.add(item);
                        }
                    }

                }
                lineNumberReader.close();
                byteArrayInputStream.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return false;
        }

        @Override
        public Model getModel() {
            // TODO Auto-generated method stub
            return mUsageStats;
        }

        @Override
        public boolean addParsedResult(String[] parsedResults) {
            // TODO Auto-generated method stub
            return false;
        }

    }
}
