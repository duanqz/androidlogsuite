package com.androidlogsuite.model.prebuild;

import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.configuration.PlotConfiguration;
import com.androidlogsuite.model.Model;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.output.Output.OutputItem;
import com.androidlogsuite.plotter.Plotter;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.Task;

public class DiskStats extends Model {

    public static String ADB_COMMAND = "shell:dumpsys diskstats";
    public static String TAG = "DiskStats";

    public long mDataTotal;
    public long mDataFree;
    public long mCacheTotal;
    public long mCacheFree;
    public long mSystemTotal;
    public long mSystemFree;

    private AdbTask mAdbClient;

    public Task getTask() {
        return mAdbClient;
    }

    public String getModelName() {
        return TAG;
    }

    public void draw(Output output) {
        String id = "diskstats";
        PlotConfiguration plotConfiguration = getProcessConfiguration()
                .getPlotConfiguration(id);

        double[] yValues = new double[] {
                (mDataFree) * 100 / (double) mDataTotal,
                (mDataTotal - mDataFree) * 100 / (double) mDataTotal,

                (mCacheFree) * 100 / (double) mCacheTotal,
                (mCacheTotal - mCacheFree) * 100 / (double) mCacheTotal,

                (mSystemFree) * 100 / (double) mSystemTotal,
                (mSystemTotal - mSystemFree) * 100 / (double) mSystemTotal, };

        String[] name = new String[] { "Data-Free", "Data-Used", "Cache-Free",
                "Cache-Used", "System-Free", "System-Used", };

        Plotter.createData(plotConfiguration, "Disk Usage", name, null, yValues);
        StringBuilder sb = Plotter.createChart(id, plotConfiguration);

        Object[] values = new Object[2];
        values[0] = new OutputItem(id, plotConfiguration.getTitle(), 300, 300);
        values[1] = sb.toString();
        output.drawObjects(values);
    }

    public DiskStats(ModelConfiguration modelConfig) {
        super(modelConfig);
        mModelParser = new DiskStatsParser(this);
        mAdbClient = new AdbTask(ADB_COMMAND, this);

    }

//    public DiskStats(String plotConfigName, int buffersize) {
//        super(plotConfigName, buffersize);
//        mModelParser = new DiskStatsParser(this);
//        mAdbClient = new AdbTask(ADB_COMMAND, this);
//    }

    static private class DiskStatsParser extends Model.ModelParser {

        private DiskStats mDiskStats;

        public DiskStatsParser(DiskStats diskStats) {
            mDiskStats = (DiskStats) diskStats;
            mParseConfiguration = mDiskStats.getProcessConfiguration()
                    .getParseConfiguration();

        }

        public Model getModel() {
            return mDiskStats;
        }

        public boolean addParsedResult(String[] parsedResults) {
            if (parsedResults[0].contains("Data")) {
                mDiskStats.mDataFree = Integer.parseInt(parsedResults[2]);
                mDiskStats.mDataTotal = Integer.parseInt(parsedResults[3]);
            } else if (parsedResults[0].contains("Cache")) {
                mDiskStats.mCacheFree = Integer.parseInt(parsedResults[2]);
                mDiskStats.mCacheTotal = Integer.parseInt(parsedResults[3]);
            } else if (parsedResults[0].contains("System")) {
                mDiskStats.mSystemFree = Integer.parseInt(parsedResults[2]);
                mDiskStats.mSystemTotal = Integer.parseInt(parsedResults[3]);
            }
            return false;
        }
    }

    public void destroy() {
        super.destroy();
    }

}
