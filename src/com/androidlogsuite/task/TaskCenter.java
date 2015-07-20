package com.androidlogsuite.task;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

public class TaskCenter implements Runnable {

    private static String TAG = TaskCenter.class.toString();
    private static Selector mSelector;
    private HashMap<SelectionKey, ISocketTask> mTasks;

    
    private static TaskCenter gTaskCenter = new TaskCenter();
    volatile private boolean mbCenterStopped = false;

    public static TaskCenter getTaskCenter() {
        return gTaskCenter;
    }

    private TaskCenter() {
        mTasks = new HashMap<SelectionKey, ISocketTask>();
        try {
            mSelector = Selector.open();
        } catch (Exception e) {
        }

    }

    public void start() {
        ThreadsPool.getThreadsPool().addTask(this);
    }

    public void stop() {
        synchronized (mTasks) {
            try {
                mbCenterStopped = true;
                mTasks.notifyAll();
                mSelector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Selector getSelector() {
        return mSelector;
    }

    public void addSocketChannel(SelectionKey key, ISocketTask client) {
        synchronized (mTasks) {
            mTasks.put(key, client);
            try {
                mTasks.notifyAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void removeSocketChannel(SelectionKey key) {
        synchronized (mTasks) {
            // it suppose AdbTask should be released before calling
            // removeSocketChannel
            mTasks.remove(key);
        }

    }

    public void run() {
        ArrayList<ISocketTask> needCloseTasks = new ArrayList<ISocketTask>();
        while (mbCenterStopped == false) {
            try {
                needCloseTasks.clear();
                // prepareToRun is protected by outside lock;
                synchronized (mTasks) {
                    if (mTasks.size() == 0) {
                        try {
                            mTasks.wait();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                    if (mbCenterStopped)
                        break;
                    Collection<ISocketTask> clients = mTasks.values();
                    for (ISocketTask client : clients) {
                        if (client.transmit() == false) {
                            needCloseTasks.add(client);
                        }
                    }
                }

                for (ISocketTask task : needCloseTasks) {
                    task.close();
                }

                mSelector.select();
                if (mbCenterStopped) {
                    break;
                }

                Iterator<SelectionKey> it = mSelector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    handleTask(key);
                    it.remove();
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        Log.d(TAG, "remove unfinished tasks");
        for (ISocketTask task : needCloseTasks) {
            task.stop();
        }
        Collection<ISocketTask> clients = mTasks.values();
        for (ISocketTask client : clients) {
            client.stop();
        }
    }

    private void handleTask(SelectionKey key) {
        ISocketTask task = null;
        synchronized (mTasks) {
            task = mTasks.get(key);
            if (task == null) {
                // it means adb tasks has been canceled
                // Log.d(TAG, "Wow, adbTask has been canceled");
                return;
            }
        }

        // task receive from target
        task.receive();
        if (task.isFinished()) {
            task.stop();
        }
    }
}
