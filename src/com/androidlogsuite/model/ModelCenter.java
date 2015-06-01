package com.androidlogsuite.model;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.configuration.ConfigCenter.FileModelConfiguration;
import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.model.prebuild.BatteryStats;
import com.androidlogsuite.model.prebuild.DiskStats;
import com.androidlogsuite.model.prebuild.MemInfo;
import com.androidlogsuite.output.OutputCenter;
import com.androidlogsuite.service.FileReadService;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.FileTask;
import com.androidlogsuite.task.ITask;
import com.androidlogsuite.task.TaskFactory;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModelCenter implements Model.ModelListener {
    private static final String TAG = "ModelCenter";

    private static ModelCenter gModelCenter = new ModelCenter();

    ArrayList<ModelConfiguration> mAdbModelConfigurations;
    ArrayList<FileModelConfiguration> mFileModelConfigurations;


    ArrayList<DynamicModel> mDynamicModels;


    private Map<Model, ITask> mAssociatedTasks;

    private Integer mParsedModelNum = 0;


    /** Whether all models have been parsed. **/
    private boolean bAllModelsParseFinished = false;

    private ModelCenter() {
        mAssociatedTasks = new HashMap<Model, ITask>();

        ConfigCenter configCenter = ConfigCenter.getConfigCenter();
        mAdbModelConfigurations = configCenter.getAdbModelConfigs();
        mFileModelConfigurations = configCenter.getFileModelConfigs();
        Log.d(TAG, "We have " + mAdbModelConfigurations.size() + " adb models to parse");
        Log.d(TAG, "We have " + mFileModelConfigurations.size() + " file models to parse");

        if (mFileModelConfigurations != null && mFileModelConfigurations.size() > 0) {
            FileReadService.getFileReadService().start();
        }

    }

    public static ModelCenter getModelCenter() {
        return gModelCenter;
    }

    public void addDynamicModel(DynamicModel model) {
        if (mDynamicModels == null)
            mDynamicModels = new ArrayList<DynamicModel>();
        synchronized (mDynamicModels) {
            mDynamicModels.add(model);
        }
    }

    public void bindModelsToTasks() {
        Model model;
        ITask task;
        // Bind adb model to task
        for (ModelConfiguration adbModelConfig : mAdbModelConfigurations) {
            model = constructModelFromModelConfig(adbModelConfig);
            task = TaskFactory.createTask(AdbTask.class);

            bindModelToTask(model, task);
        }

        // Bind file model to task
        for (FileModelConfiguration fileModelConfig : mFileModelConfigurations) {
            // Start the service to process file
            for (ModelConfiguration adbModelConfig : fileModelConfig.getFileModels()) {
                model = constructModelFromModelConfig(adbModelConfig);
                FileTask fileTask = (FileTask) TaskFactory.createTask(FileTask.class);
                // Each file task has a related filename
                fileTask.setFileName(fileModelConfig.mFileName);

                bindModelToTask(model, fileTask);
            }
        }
    }

    private void bindModelToTask(Model model, ITask task) {
        if (model == null || task == null) {
            Log.d(TAG, "Fail to bind, model or task is null");
            return;
        }

        task.start(model);
        mAssociatedTasks.put(model, task);
    }

    private Model constructModelFromModelConfig(ModelConfiguration modelConfig) {
        Model model = null;
        ITask task;
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

        return model;
    }

    @Override
    public void onModelParsed(Model model) {
        synchronized (mParsedModelNum) {
            mParsedModelNum += 1;
        }

        if (model.hasOutputData()) {
            Log.d(TAG, model + " have been parsed, add to OutputCenter");
            OutputCenter.getOutputCenter().addModel(model);
        } else {
            Log.d(TAG, model + " have been parsed, but no output data");
        }

        if (mParsedModelNum == mAssociatedTasks.size()) {
            Log.d(TAG, "All models have been parsed, quit ModelCenter");
            mAssociatedTasks.clear();

            // Add null object to tell OutputCenter quit.
            OutputCenter.getOutputCenter().addModel(null);
        }
    }

}
