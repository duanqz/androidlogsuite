package com.androidlogsuite.task;

import com.androidlogsuite.model.Model;
import com.androidlogsuite.util.Log;

/**
 * Task factory to create task
 *
 * @author duanqizhi
 */
public class TaskFactory {

    private static final String TAG = "TaskFactory";

    public static <T extends ITask> ITask createTask(Class<T> c) {
        T task = null;
        try {
            task = c.newInstance();

        } catch (InstantiationException e) {
            Log.d(TAG, "Can not initiate " + c);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.d(TAG, "Can not initiate " + c);
            e.printStackTrace();
        }
        return task;
    }
}
