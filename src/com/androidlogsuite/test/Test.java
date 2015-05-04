package com.androidlogsuite.test;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.model.ModelCenter;
import com.androidlogsuite.output.OutputCenter;
import com.androidlogsuite.output.OutputCenter.OutputCenterCallback;
import com.androidlogsuite.task.TaskCenter;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

public class Test implements OutputCenterCallback {

    /**
     * @param args
     */
    public static void main(String[] args) {

        // testJFreeChart();
        testAdb();
        // testpdf();
        // testXMLParse();
    }

    private static void testAdb() {
        // DiskStats.test();
        //Test test = new Test();
        //ThreadsPool.getThreadsPool().start();
        //TaskCenter.getTaskCenter().start();
        //OutputCenter.getOutputCenter().setOutputCenterCallback(test).start();
        ConfigCenter.getConfigCenter().setupConfig();
        ModelCenter.getModelCenter().runModels();
        /*synchronized (test) {
            try {
                test.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TaskCenter.getTaskCenter().stop();
        OutputCenter.getOutputCenter().stop();
        ThreadsPool.getThreadsPool().stop();*/

        Log.d("Test", "finished work, quit test");
    }

    @Override
    public void outputCenterQuit() {
        synchronized (this) {
            this.notify();
        }

    }

}
