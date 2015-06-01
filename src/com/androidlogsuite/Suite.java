package com.androidlogsuite;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.model.ModelCenter;
import com.androidlogsuite.output.OutputCenter;
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

        // Setup model thread to run tasks associated with models
        ModelCenter.getModelCenter().runModels();

    }

    @Override
    public void outputCenterQuit() {
        TaskCenter.getTaskCenter().stop();
        OutputCenter.getOutputCenter().stop();
        ThreadsPool.getThreadsPool().stop();

        Log.d(TAG, "Finish output.");
    }

    public static void main(String[] args) {
        new Suite().setup();
    }
}
