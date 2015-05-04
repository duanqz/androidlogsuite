package com.androidlogsuite.model;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.configuration.ConfigCenter.FileModelConfiguration;
import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.model.prebuild.BatteryStats;
import com.androidlogsuite.model.prebuild.DiskStats;
import com.androidlogsuite.model.prebuild.MemInfo;
import com.androidlogsuite.output.OutputCenter;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.util.Log;

public class ModelCenter implements Model.ModelListener {
    private static final String TAG = "ModelCenter";

    private static ModelCenter gModelCenter = new ModelCenter();

   ArrayList<ModelConfiguration> mAdbModelConfigurations;
   ArrayList<FileModelConfiguration> mFileModelConfigurations;

  

    private int mParsedModelIndex = 0;

    private ModelCenter() {
    }

    public static ModelCenter getModelCenter() {
        return gModelCenter;
    }

   /* public void addModel(Model model) {
        synchronized (mModels) {
            mModels.add(model);
        }
    }

    public void addDynamicModel(DynamicModel model) {
        if (mDynamicModels == null)
            mDynamicModels = new ArrayList<DynamicModel>();
        synchronized (mDynamicModels) {
            mDynamicModels.add(model);
        }

    }*/

    public void runModels() {
        ConfigCenter configCenter = ConfigCenter.getConfigCenter();
        mAdbModelConfigurations = configCenter.getAdbModelConfigs();
        Log.d(TAG, "We have " + mAdbModelConfigurations.size() + " adb models to parse");
        mFileModelConfigurations = configCenter.getFileModelConfigs();
        Log.d(TAG, "We have " + mFileModelConfigurations.size() + " file models to parse");

    }

 /*   private void constructModelFromModelConfig(ModelConfig modelConfig) {
        Model model = null;
        if (modelConfig.mType.equals("logcat")) {
            model = new DynamicModel(AdbTask.getLogcatTask(modelConfig.mCmd,
                    modelConfig.mbPrintTime), modelConfig);
        } else if (modelConfig.mType.equals("dumpsys")) {
            String cmd = modelConfig.mCmd;
            if (cmd.equals("diskstats")) {
                model = new DiskStats(modelConfig.mPlotConfigName,
                        modelConfig.mBufferSize);
            } else if (cmd.equals("meminfo")) {
                model = new MemInfo(modelConfig.mPlotConfigName,
                        modelConfig.mBufferSize);
            } else if (cmd.equals("batterystats")) {
                model = new BatteryStats(modelConfig.mPlotConfigName,
                        modelConfig.mBufferSize);
            }
        }

        if (modelConfig.mDebug)
            model.mbPrintVerbose = true;
    }*/

    public void parseFinished(Model model) {
       /* boolean bFinished = false;
        ArrayList<Model> addedModels = new ArrayList<Model>();
        synchronized (mModels) {
            for (int i = mParsedModelIndex; i < mModels.size(); i++, mParsedModelIndex++) {
                Model existingModel = mModels.get(i);
                if (existingModel.mbParseFinised) {
                    addedModels.add(existingModel);

                } else
                    break;
            }

            if (mParsedModelIndex == mModels.size()) {
                Log.d(TAG, "all static models have been parsed");
                bFinished = true;
                if (mDynamicModels == null || mDynamicModels.size() == 0) {
                    Log.d(TAG, "no dynamic models is running");

                } else {
                    Log.d(TAG,
                            "dynamic models is running,can not quit model center");
                }
            }
        }
        if (addedModels.size() != 0) {
            OutputCenter.getOutputCenter().addModels(addedModels);
            addedModels.clear();
            addedModels = null;
        }
        if (bFinished) {
            Log.d(TAG, "quit model center");
            mModels.clear();
            OutputCenter.getOutputCenter().addModel(null);
        }*/

    }

}
