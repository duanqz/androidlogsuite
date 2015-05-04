package com.androidlogsuite.output;

import java.util.ArrayList;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.model.Model;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

public class OutputCenter {

    private static final String TAG = OutputCenter.class.toString();
    private ArrayList<Model> mModels;
    private static OutputCenter gCenter = new OutputCenter();
    private OutputRunnable mOutputRunnable;

    private Output mOutput;
    private OutputCenterCallback mOutputCenterCallback;

    static public interface OutputCenterCallback {
        public void outputCenterQuit();
    }

    private OutputCenter() {
        mModels = new ArrayList<Model>();
    }

    public static OutputCenter getOutputCenter() {
        return gCenter;
    }

    public OutputCenter setOutputCenterCallback(
            OutputCenterCallback outputCenterCallback) {
        mOutputCenterCallback = outputCenterCallback;
        return gCenter;
    }

    public void start() {
        if (mOutputRunnable == null) {
            mOutputRunnable = new OutputRunnable();
            ThreadsPool threadsPool = ThreadsPool.getThreadsPool();
            threadsPool.addTask(mOutputRunnable);
        }
    }

    public void stop() {
        synchronized (mModels) {
            mModels.clear();
            mModels.notifyAll();
        }
        mOutputRunnable = null;
    }

    public void addModel(Model model) {
        synchronized (mModels) {
            mModels.add(model);
            mModels.notifyAll();
        }
    }

    public void addModels(ArrayList<Model> model) {
        synchronized (mModels) {
            mModels.addAll(model);
            mModels.notifyAll();
        }
    }

    private void prepardOutput() {
        if (mOutput == null) {
            mOutput = Output.getOutputByType(ConfigCenter.getConfigCenter()
                    .getOutputType());
            boolean bSuc = mOutput.createOutput();
            Log.d(TAG, "create output  " + bSuc);
        }
        return;

    }

    private class OutputRunnable implements Runnable {
        public void run() {
            prepardOutput();
            while (ThreadsPool.isThreadsPoolStopped() == false) {
                Model outputModel = null;
                synchronized (mModels) {
                    if (mModels.size() == 0) {
                        try {
                            mModels.wait();
                        } catch (Exception exception) {

                            break;
                        }
                    }
                    outputModel = mModels.remove(0);
                    if (outputModel == null) {
                        mOutput.closeOutput();
                        mOutput = null;
                        break;
                    }
                    outputModel.draw(mOutput);

                    outputModel.destroy();

                }
            }

            Log.d(TAG, "Quit output center");
            if (mOutputCenterCallback != null) {
                mOutputCenterCallback.outputCenterQuit();
            }
        }

    }
}
