package com.androidlogsuite.model.prebuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.configuration.PlotConfiguration;
import com.androidlogsuite.configuration.PlotConfiguration.DataFilter;
import com.androidlogsuite.model.Model;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.output.Output.OutputItem;
import com.androidlogsuite.plotter.Plotter;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.Task;
import com.androidlogsuite.util.Log;

public class MemInfo extends Model {
    // shell:export ANDROID_LOG_TAGS=\"\"; exec logcat
    public static String ADB_COMMAND = "shell:dumpsys meminfo -c";
    public static String TAG = "MemInfo";

    private long mUpTime;
    private long mRealTime;

    private static class ProcessMemInfo {
        long totalPSS; // unit is KB
        String processName;
        String adjType;

    }

    private static final int INT_AdjCached = 12;
    private static final int INT_Adj_COUNT = INT_AdjCached + 1;

    private static final String[] CATEGORY = { "native", "sys", "pers", "fore",
            "vis", "percept", "heavy", "backup", "servicea", "home", "prev",
            "serviceb", "cached" };
    private static final String[] CATEGORY_FULL = { "Native", "System",
            "Persistent", "Foreground", "Visible", "Perceptible",
            "Heavy Weight", "Backup", "Services A", "Home", "Previous",
            "Services B", "Cached" };

    private LinkedList<ProcessMemInfo>[] mTotalPssByAdj;
    private long[] mPssByCategory = null;

    private class TotalRamInfo {
        private int totalRam;
        private int freeRam;
        private int usedRam;
    }

    private TotalRamInfo mTotalRamInfo;

    private Task mAdbClient;

    @Override
    public Task getTask() {
        return mAdbClient;
    }

    public String getModelName() {
        return TAG;
    }

    @Override
    public void draw(Output output) {
        Log.d(TAG, "MemInfo draw");
        drawTotalInfo(output);
        drawCaterygoryInfo(output);
        for (int i = 0; i < mTotalPssByAdj.length; i++) {
            LinkedList<ProcessMemInfo> memInfos = mTotalPssByAdj[i];
            if (memInfos == null || memInfos.size() == 0)
                continue;
            drawCategory(output, memInfos, i);
        }

    }

    private void drawTotalInfo(Output output) {
        double[] yValues = {
                (double) mTotalRamInfo.usedRam * 100
                        / (double) mTotalRamInfo.totalRam,
                (double) mTotalRamInfo.freeRam * 100
                        / (double) mTotalRamInfo.totalRam };
        String[] name = { "Used-Ram", "Free-Ram" };

        String id = "total";

        PlotConfiguration plotConfiguration = getProcessConfiguration()
                .getPlotConfiguration(id);

        Plotter.createData(plotConfiguration, "Total Memory Info", name, null,
                yValues);
        StringBuilder sb = Plotter.createChart(id, plotConfiguration);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfiguration.getTitle(), 200, 200);
        values[1] = sb.toString();
        output.drawObjects(values);
    }

    private void drawCaterygoryInfo(Output output) {
        String id = "category";
        PlotConfiguration plotConfig = getProcessConfiguration().getPlotConfiguration(
                id);

        int[] inis = Plotter.sort(mPssByCategory,
                plotConfig.mDataFilter.mSortOrder);

        ArrayList<String> nameList = new ArrayList<String>();
        ArrayList<Double> valuesList = new ArrayList<Double>();
        for (int i = 0; i < inis.length; i++) {
            double value = mPssByCategory[inis[i]] >> 10;
            if (value < plotConfig.mDataFilter.mWaterMark)
                continue;
            nameList.add(CATEGORY_FULL[inis[i]]);
            valuesList.add(value);
        }

        Plotter.createAxis(plotConfig, nameList, true);

        String[] names = new String[nameList.size()];
        double[] yValues = new double[valuesList.size()];

        nameList.toArray(names);
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = valuesList.get(i).doubleValue();
        }

        Plotter.createData(plotConfig, "Memory By Category", names, null,
                yValues);
        StringBuilder sb = Plotter.createChart(id, plotConfig);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfig.getTitle(), 400, 400);
        values[1] = sb.toString();
        output.drawObjects(values);

    }

    private class MeminfoComparator implements Comparator<Object> {
        private int mSortOrder = -1;

        public MeminfoComparator(int sortOrder) {
            mSortOrder = sortOrder;
        }

        private int getComparisionResult(boolean leftLessThanRight) {
            if (mSortOrder == -1 || mSortOrder == DataFilter.SORT_ORDER_DESCEND)
                return leftLessThanRight ? 1 : -1;
            else
                return leftLessThanRight ? -1 : 1;
        }

        @Override
        public int compare(Object arg0, Object arg1) {
            if (arg0 == null) {
                return getComparisionResult(true);
            }
            if (arg1 == null) {
                return getComparisionResult(false);
            }
            if (arg0 instanceof ProcessMemInfo) {
                ProcessMemInfo memInfo0 = (ProcessMemInfo) arg0;
                ProcessMemInfo memInfo1 = (ProcessMemInfo) arg1;
                return getComparisionResult(memInfo0.totalPSS < memInfo1.totalPSS);

            }
            return 0;
        }

    }

    private void drawCategory(Output output,
            LinkedList<ProcessMemInfo> memInfos, int index) {
        String id = CATEGORY[index];
        PlotConfiguration plotConfiguration = getProcessConfiguration()
                .getPlotConfiguration(id);
        if (plotConfiguration == null)
            return;

        Collections.sort(memInfos, new MeminfoComparator(
                plotConfiguration.mDataFilter.mSortOrder));

        ArrayList<String> nameList = new ArrayList<String>();
        ArrayList<Double> valuesList = new ArrayList<Double>();

        for (ProcessMemInfo memInfo : memInfos) {
            long pssInMB = memInfo.totalPSS / (1 << 10);
            if (pssInMB < plotConfiguration.mDataFilter.mWaterMark)
                continue;
            nameList.add(memInfo.processName);
            valuesList.add((double) pssInMB);
        }
        if (nameList.size() == 0)
            return;
        Plotter.createAxis(plotConfiguration, nameList, true);

        String[] names = new String[nameList.size()];
        double[] yValues = new double[valuesList.size()];

        nameList.toArray(names);
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = valuesList.get(i).doubleValue();
        }

        Plotter.createData(plotConfiguration, "Processes PSS in : "
                + CATEGORY_FULL[index], names, null, yValues);
        StringBuilder sb = Plotter.createChart(id, plotConfiguration);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfiguration.getTitle(), 300, 400);
        values[1] = sb.toString();
        output.drawObjects(values);
        return;

    }

    public MemInfo(ModelConfiguration modelConfig) {
        super(modelConfig);
        mAdbClient = new AdbTask(ADB_COMMAND, this);
        mModelParser = new MemInfoParser(this);

    }

//    public MemInfo(String plotConfig, int buffersize) {
//        super(plotConfig, buffersize);
//        mAdbClient = new AdbTask(ADB_COMMAND, this);
//        mModelParser = new MemInfoParser(this);
//
//    }

    @Override
    public void destroy() {
        super.destroy();

    }

    private static int getIndexFromCategory(String category) {
        int index = -1;
        for (String str : CATEGORY) {
            index++;
            if (str.equals(category))
                return index;
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    private void createModelData() {
        if (mTotalPssByAdj != null)
            return;
        mTotalPssByAdj = (LinkedList<ProcessMemInfo>[]) new LinkedList[INT_Adj_COUNT];
        for (int i = 0; i < INT_Adj_COUNT; i++) {
            mTotalPssByAdj[i] = new LinkedList<ProcessMemInfo>();
        }
        mTotalRamInfo = new TotalRamInfo();
        mPssByCategory = new long[INT_Adj_COUNT];
    }

    static public class MemInfoParser extends Model.ModelParser {
        private MemInfo mMemInfo;

        private static final String LINE_BEGINWITH_TIME = "time";
        private static final String LINE_BEGINWITH_PROC = "proc";
        private static final String LINE_BEGINWITH_OOM = "oom";
        private static final String LINE_BEGINWITH_RAM = "ram";

        public MemInfoParser(MemInfo model) {
            mMemInfo = model;
            mParseConfiguration = mMemInfo.getProcessConfiguration()
                    .getParseConfiguration();
        }

        public Model getModel() {
            return mMemInfo;
        }

        @Override
        public boolean addParsedResult(String[] parsedResults) {
            mMemInfo.createModelData();
            String startsWithWord = mParseConfiguration.getCachedKeyWord();
            if (startsWithWord == null) {
                return false;
            }
            if (startsWithWord.contains(LINE_BEGINWITH_TIME)) {
                mMemInfo.mUpTime = Integer.parseInt(parsedResults[0]);
                mMemInfo.mRealTime = Integer.parseInt(parsedResults[1]);
            } else if (startsWithWord.contains(LINE_BEGINWITH_PROC)) {
                ProcessMemInfo memInfo = new ProcessMemInfo();
                memInfo.adjType = parsedResults[0];
                memInfo.processName = parsedResults[1];
                memInfo.totalPSS = Integer.parseInt(parsedResults[3]);
                LinkedList<ProcessMemInfo> list = mMemInfo.mTotalPssByAdj[getIndexFromCategory(memInfo.adjType)];
                list.add(memInfo);
            } else if (startsWithWord.contains(LINE_BEGINWITH_OOM)) {
                String type = parsedResults[0];
                long pss = Integer.parseInt(parsedResults[1]);
                mMemInfo.mPssByCategory[getIndexFromCategory(type)] = pss;
            } else if (startsWithWord.contains(LINE_BEGINWITH_RAM)) {
                mMemInfo.mTotalRamInfo.totalRam = Integer
                        .parseInt(parsedResults[0]);
                mMemInfo.mTotalRamInfo.freeRam = Integer
                        .parseInt(parsedResults[1]);
                mMemInfo.mTotalRamInfo.usedRam = Integer
                        .parseInt(parsedResults[2]);
                return true;
            }
            return false;
        }
    }
}
