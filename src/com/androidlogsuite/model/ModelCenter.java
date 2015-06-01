package com.androidlogsuite.model;

import java.util.ArrayList;

import com.androidlogsuite.output.Output;
import com.androidlogsuite.task.TaskCenter;
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


    ArrayList<DynamicModel> mDynamicModels = null;


    private ArrayList<Model> mAllModels = new ArrayList<Model>();
    private Integer mParsedModelNum = 0;


    /** Whether all models have been parsed. **/
    private boolean bAllModelsParseFinished = false;

    private ModelCenter() {
    }

    public static ModelCenter getModelCenter() {
        return gModelCenter;
    }

    public void addModel(Model model) {
        synchronized (mAllModels) {
            mAllModels.add(model);
        }
    }

    public void addDynamicModel(DynamicModel model) {
        if (mDynamicModels == null)
            mDynamicModels = new ArrayList<DynamicModel>();
        synchronized (mDynamicModels) {
            mDynamicModels.add(model);
        }

    }

    public void runModels() {
        ConfigCenter configCenter = ConfigCenter.getConfigCenter();
        mAdbModelConfigurations = configCenter.getAdbModelConfigs();
        Log.d(TAG, "We have " + mAdbModelConfigurations.size() + " adb models to parse");
        mFileModelConfigurations = configCenter.getFileModelConfigs();
        Log.d(TAG, "We have " + mFileModelConfigurations.size() + " file models to parse");


        for (ModelConfiguration modelConfig : mAdbModelConfigurations) {
            Model model = constructModelFromModelConfig(modelConfig);
            if (model != null) {
                //model.mbPrintVerbose = true;
                mAllModels.add(model);
            }
//            if (modelConfig.mType.equals("dumpsys") && modelConfig.mCmd.equals("meminfo")) {
//                Model model = new MemInfo(modelConfig);
//                model.mbPrintVerbose = true;
//                mAllModels.add(model);
//            }
        }
    }

    private Model constructModelFromModelConfig(ModelConfiguration modelConfig) {
        Model model = null;
//        if (modelConfig.mType.equals("logcat")) {
//            //model = new DynamicModel(AdbTask.getLogcatTask(modelConfig.mCmd, modelConfig.mbPrintTime));
//            model = new DynamicModel(modelConfig);
//        } else
        if (modelConfig.mType.equals("dumpsys")) {
            String cmd = modelConfig.mCmd;
            if (cmd.equals("diskstats")) {
                model = new DiskStats(modelConfig);
            } else if (cmd.equals("meminfo")) {
                model = new MemInfo(modelConfig);
            } else if (cmd.equals("batterystats")) {
                model = new BatteryStats(modelConfig);
            }
        }

//        if (modelConfig.mDebug)
//            model.mbPrintVerbose = true;

        return model;
    }

    public void parseFinished(Model model) {
        //boolean bFinished = false;
        //ArrayList<Model> parseFinishedModels = new ArrayList<Model>();
//        synchronized (mAllModels) {
//            for (int i = mParsedModelIndex; i < mModels.size(); i++, mParsedModelIndex++) {
//                Model existingModel = mModels.get(i);
//                if (existingModel.mbParseFinised) {
//                    parseFinishedModels.add(existingModel);
//                } else
//                    break;
//            }
//
//            if (mParsedModelIndex == mModels.size()) {
//                Log.d(TAG, "all static models have been parsed");
//                bFinished = true;
//                if (mDynamicModels == null || mDynamicModels.size() == 0) {
//                    Log.d(TAG, "no dynamic models is running");
//
//                } else {
//                    Log.d(TAG, "dynamic models is running,can not quit model center");
//                }
//            }
//        }
//
//
//
//        if (parseFinishedModels.size() != 0) {
//            OutputCenter.getOutputCenter().addModels(parseFinishedModels);
//            parseFinishedModels.clear();
//            parseFinishedModels = null;
//        }
//        if (bFinished) {
//            Log.d(TAG, "quit model center");
//            mModels.clear();
//            OutputCenter.getOutputCenter().addModel(null);
//        }

        synchronized (mParsedModelNum) {
            if (model.mbParseFinised) {
                mParsedModelNum += 1;
            }
        }

        if (model.mbParseFinised) {
            Log.d(TAG, model + " have been parsed, add to OutputCenter");
            OutputCenter.getOutputCenter().addModel(model);
        }

        if (mParsedModelNum == mAllModels.size()) {
            Log.d(TAG, "All models have been parsed, quit ModelCenter");
            mAllModels.clear();

            // Add null object to force OutputCenter quit.
            OutputCenter.getOutputCenter().addModel(null);
        }
    }

}
