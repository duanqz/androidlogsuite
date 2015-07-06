package com.androidlogsuite.model;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.configuration.ConfigCenter.FileModelConfiguration;
import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.model.prebuild.BatteryStats;
import com.androidlogsuite.model.prebuild.DiskStats;
import com.androidlogsuite.model.prebuild.Logcat;
import com.androidlogsuite.model.prebuild.MemInfo;
import com.androidlogsuite.output.OutputCenter;
import com.androidlogsuite.service.FileReadService;
import com.androidlogsuite.task.AdbTask;
import com.androidlogsuite.task.FileTask;
import com.androidlogsuite.task.ITask;
import com.androidlogsuite.task.TaskFactory;
import com.androidlogsuite.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModelCenter implements Model.ModelListener {
    private static final String TAG = "ModelCenter";

    private static ModelCenter gModelCenter = new ModelCenter();

    ArrayList<ModelConfiguration> mAdbModelConfigurations;
    ArrayList<FileModelConfiguration> mFileModelConfigurations;

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

    public void bindModelsToTasks() {

        // Bind adb model to task
        for (ModelConfiguration adbModelConfig : mAdbModelConfigurations) {
            Model model = constructModelFromModelConfig(adbModelConfig);
            AdbTask task = (AdbTask) TaskFactory.createTask(AdbTask.class);

            bindModelToTask(model, task);
        }

        // Bind file model to task
        for (FileModelConfiguration fileModelConfig : mFileModelConfigurations) {
            // Start the service to process file
            for (ModelConfiguration adbModelConfig : fileModelConfig.getFileModels()) {
                Model model = constructModelFromModelConfig(adbModelConfig);
                FileTask fileTask = (FileTask) TaskFactory.createTask(FileTask.class);
                // Each file task has a related filename
                fileTask.setFileName(fileModelConfig.mFileName);

                bindModelToTask(model, fileTask);
            }
        }

        // Force to exit if no task
        if (mAssociatedTasks.size() == 0) {
            // Add null object to tell OutputCenter quit.
            OutputCenter.getOutputCenter().addModel(null);
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
        if (modelConfig.mType.equals("dumpsys")) {
            String cmd = modelConfig.mCmd;
            if (cmd.equals("diskstats")) {
                model = new DiskStats(modelConfig);
            } else if (cmd.equals("meminfo")) {
                model = new MemInfo(modelConfig);
            } else if (cmd.equals("batterystats")) {
                model = new BatteryStats(modelConfig);
            }
        } else if (modelConfig.mType.equals("logcat")) {
            String cmd = modelConfig.mCmd;
            if ("main".equals(cmd) ||
                "system".equals(cmd) ||
                "radio".equals("cmd") ||
                "events".equals(cmd) ||
                "all".equals(cmd)) {

                //model = new Logcat(modelConfig);
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
