package com.androidlogsuite.model.prebuild;

import java.util.ArrayList;

import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.configuration.PlotConfiguration;
import com.androidlogsuite.configuration.ParseConfiguration.ParseRule;
import com.androidlogsuite.model.Model;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.output.Output.OutputItem;
import com.androidlogsuite.plotter.Plotter;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.ITask;
import com.androidlogsuite.task.Task;
import com.androidlogsuite.util.Log;

public class BatteryStats extends Model {

    private static final String TAG = "BatteryStats";
    public static String ADB_COMMAND = "shell:dumpsys batterystats -c";

    public BatteryStats(ModelConfiguration modelConfig) {
        super(modelConfig);
        mModelParser = new BatteryStatsParser(this);
    }

//    public BatteryStats(String plotConfig, int buffersize) {
//        super(plotConfig, buffersize);
//        mModelParser = new BatteryStatsParser(this);
//        mAdbClient = new AdbTask(ADB_COMMAND, this);
//
//    }

    static public class BatteryHistoryItem {
        long mTime;
        int batteryLevel;
    }

    static public class APKUsageItem {
        public APKUsageItem(String[] parseValues) {
            wakeups = Integer.parseInt(parseValues[0]);
            mApkName = parseValues[1];
            mServiceName = parseValues[2];
            mStartTime = Long.parseLong(parseValues[3]);
            mStartCounts = Long.parseLong(parseValues[4]);
            mLaunches = Long.parseLong(parseValues[5]);
        }

        int wakeups;// wakeup alarms
        String mApkName; // Apk
        String mServiceName;
        long mStartTime; // time spent started, in ms
        long mStartCounts;
        long mLaunches;
    }

    private static final int MISC_SCREENON_TIME = 0;
    private static final int MISC_PHONEON_TIME = 1;
    private static final int MISC_WIFION_TIME = 2;
    private static final int MIS_WIFIRUNNING_TIME = 3;
    private static final int MISC_BTONT_TIME = 4;
    private static final int MISC_MOBILERX_TOTAL = 5;
    private static final int MISC_MOBILETX_TOTAL = 6;
    private static final int MISC_WIFIRX_TOTAL = 7;
    private static final int MISC_WIFITX_TOTAL = 8;
    private static final int MISC_FWLTIME_TOTAL = 9;
    private static final int MISC_PWLTIME_TOTAL = 10;
    private static final int MISC_INTPUTEVENT_TOTAL = 11;
    private static final int MISC_COUNT = 12;
    private long[] mMisItem;

    private ArrayList<BatteryHistoryItem> mBatteryHistoryItems;
    private ArrayList<APKUsageItem> mApkUsageItems;

    @Override
    public String getModelName() {
        return TAG;
    }

    @Override
    public String getAdbCommand() {
        return ADB_COMMAND;
    }

    @Override
    public void draw(Output output) {
        Log.d(TAG, "BatteryStats draw");

        drawHistory(output);
        drawDataUsage(output);
    }

    private void drawHistory(Output output) {
        String id = "history";
        PlotConfiguration plotConfig = getProcessConfiguration().getPlotConfiguration(
                id);
        if (plotConfig == null)
            return;
        int N = (int) Math.ceil((double) mBatteryHistoryItems.size()
                / (double) 1000);
        if (N == 0)
            N = 1;
        ArrayList<Double> xDoubles = new ArrayList<Double>();
        ArrayList<Double> yDoubles = new ArrayList<Double>();
        boolean timeisPositive = false;
        for (int i = 0; i < mBatteryHistoryItems.size(); i = i + N) {
            BatteryHistoryItem item = mBatteryHistoryItems.get(i);
            xDoubles.add((double) item.mTime);
            if (timeisPositive == false && item.mTime >= 0)
                timeisPositive = true;
            yDoubles.add((double) item.batteryLevel);
        }

        Plotter.createData(plotConfig, "Battery History Info", null,
                timeisPositive ? xDoubles : null, yDoubles);
        StringBuilder sb = Plotter.createChart(id, plotConfig);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfig.getTitle(), 400, 400);
        values[1] = sb.toString();
        output.drawObjects(values);
        return;
    }

    private void drawDataUsage(Output output) {
        String id = "datausage";
        PlotConfiguration plotConfig = getProcessConfiguration().getPlotConfiguration(
                id);
        if (plotConfig == null)
            return;

        ArrayList<String> nameList = new ArrayList<String>();
        ArrayList<Double> valueList = new ArrayList<Double>();

        long value = mMisItem[MISC_MOBILERX_TOTAL] >> 10;
        if (value != 0) {
            nameList.add("Mobile RX");
            valueList.add((double) value);
        }

        value = mMisItem[MISC_MOBILETX_TOTAL] >> 10;
        if (value != 0) {
            nameList.add("Mobile TX");
            valueList.add((double) value);
        }

        value = mMisItem[MISC_WIFIRX_TOTAL] >> 10;
        if (value != 0) {
            nameList.add("Wi-Fi RX");
            valueList.add((double) value);
        }

        value = mMisItem[MISC_WIFITX_TOTAL] >> 10;
        if (value != 0) {
            nameList.add("Wi-Fi TX");
            valueList.add((double) value);
        }

        if (nameList.size() == 0)
            return;
        Plotter.createData(plotConfig, "Data Usage Info", nameList, null,
                valueList);
        StringBuilder sb = Plotter.createChart(id, plotConfig);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfig.getTitle(), 400, 400);
        values[1] = sb.toString();
        output.drawObjects(values);
    }

    private void createModelData() {
        if (mBatteryHistoryItems != null) {
            return;
        }
        mBatteryHistoryItems = new ArrayList<BatteryHistoryItem>();
        mApkUsageItems = new ArrayList<APKUsageItem>();
        mMisItem = new long[MISC_COUNT];
    }

    static public class BatteryStatsParser extends ModelParser {

        private BatteryStats mBatteryStats;

        private static final String LINE_BEGINNWITH_HISTORY_KK = "7,0,h";
        private static final String LINE_BEGINNWITH_HISTORY_L = "9,h";
        private static final String LINE_BEIGGINWITH_MISC_KK = "7,0,t,m";
        private static final String LINE_BEIGGINWITH_MISC_L = "9,0,l,m";

        private static final String LINE_TOTAL_APK_USAGE_KK = "(?:7,\\d+,t,apk,)(.+)";
        private static final String LINE_TOTAL_APK_USAGE_L = "(?:9,\\d+,l,apk,)(.+)";

        private static final String LINE_WITH_END_KK = "7,0,u";
        private static final String LINE_WITH_END_L = "9,0,u";

        public BatteryStatsParser(Model stats) {
            mBatteryStats = (BatteryStats) stats;
            mParseConfiguration = mBatteryStats.getProcessConfiguration()
                    .getParseConfiguration();

        }

        public Model getModel() {
            return mBatteryStats;
        }

        @Override
        public boolean addParsedResult(String[] parsedResults) {
            mBatteryStats.createModelData();
            String startsWithWord = mParseConfiguration.getCachedKeyWord();
            if (startsWithWord == null) {
                ParseRule cachedParseRule = mParseConfiguration
                        .getCachedParseRule();
                if (cachedParseRule == null)// no cached rule
                    return false;
                String line = parsedResults[0];
                Log.d(TAG, "original data is : " + line);
                if (line.matches(LINE_TOTAL_APK_USAGE_KK)
                        || line.matches(LINE_TOTAL_APK_USAGE_L)) {
                    String apkUsage = parsedResults[1];
                    String values[] = apkUsage.split(",");
                    APKUsageItem apkUsageItem = new APKUsageItem(values);
                    mBatteryStats.mApkUsageItems.add(apkUsageItem);
                    return false;
                }
            }
            if (startsWithWord.startsWith(LINE_BEGINNWITH_HISTORY_KK)
                    || startsWithWord.startsWith(LINE_BEGINNWITH_HISTORY_L)) {
                BatteryHistoryItem item = new BatteryHistoryItem();
                item.mTime = Long.parseLong(parsedResults[0]) / 1000;
                item.batteryLevel = Integer.parseInt(parsedResults[1]);
                mBatteryStats.mBatteryHistoryItems.add(item);
            } else if (startsWithWord.startsWith(LINE_BEIGGINWITH_MISC_KK)
                    || startsWithWord.startsWith(LINE_BEIGGINWITH_MISC_L)) {
                String misc = parsedResults[0];
                String values[] = misc.split(",");
                for (int i = MISC_SCREENON_TIME; i < MISC_COUNT; i++) {
                    mBatteryStats.mMisItem[i] = Long.parseLong(values[i]);
                }
            } else if (startsWithWord.startsWith(LINE_WITH_END_KK)
                    || startsWithWord.startsWith(LINE_WITH_END_L)) {
                return true;
            }
            return false;
        }

    }

}
