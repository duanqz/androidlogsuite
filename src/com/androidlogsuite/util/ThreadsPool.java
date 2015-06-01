package com.androidlogsuite.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadsPool {

    private static final String TAG = ThreadsPool.class.toString();

    private static final int MAX_THREADS = 10;
    ExecutorService mExecutorService = null;

    private static ThreadsPool gThreadsPool = new ThreadsPool();
    private static volatile boolean gThreadStopped = false;

    private ThreadsPool() {

    }

    public static ThreadsPool getThreadsPool() {
        return gThreadsPool;
    }

    public synchronized Future<?> addTask(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    public synchronized void start() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(MAX_THREADS);
        }
        gThreadStopped = false;
        return;
    }

    public synchronized void stop() {
        gThreadStopped = true;
        shutdownAndAwaitTermination();
        mExecutorService = null;
    }

    public static boolean isThreadsPoolStopped() {
        return gThreadStopped;
    }

    private void shutdownAndAwaitTermination() {
        mExecutorService.shutdown();
        try {
            Log.d(TAG, "Waiting 60s ...");
            if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                mExecutorService.shutdownNow();
                if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS))
                    Log.d(TAG, "Pool did not terminate");
            }

        } catch (InterruptedException ie) {
            mExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
