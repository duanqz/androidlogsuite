package com.androidlogsuite;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.model.ModelCenter;
import com.androidlogsuite.output.OutputCenter;
import com.androidlogsuite.service.FileReadService;
import com.androidlogsuite.task.TaskCenter;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

/**
 * Entry
 *
 * @author duanqz@gmail.com
 */
public class Suite implements OutputCenter.OutputCenterCallback {

    private static final String TAG = "Suite";

    public void setup() {
        // Setup thread pool
        ThreadsPool.getThreadsPool().start();

        // Setup configurations from XML
        ConfigCenter.getConfigCenter().setupConfig();

        // Setup output thread waiting for parsed models
        OutputCenter.getOutputCenter().setOutputCenterCallback(this).start();

        // Setup task thread waiting for parsing task
        TaskCenter.getTaskCenter().start();

        // Setup model center to bind models to tasks
        // This will trigger the whole process
        ModelCenter.getModelCenter().bindModelsToTasks();

    }

    @Override
    public void outputCenterQuit() {
        Log.d(TAG, ">>> OutputCenter is quit, stopping all threads ...");
        FileReadService.getFileReadService().stop();
        TaskCenter.getTaskCenter().stop();
        OutputCenter.getOutputCenter().stop();
        ThreadsPool.getThreadsPool().stop();

        Log.d(TAG, "<<< finish stopping all threads.");
    }

    public static void main(String[] args) {
        new Suite().setup();
    }
}
