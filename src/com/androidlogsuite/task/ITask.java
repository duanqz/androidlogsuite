package com.androidlogsuite.task;

import com.androidlogsuite.model.Model;

/**
 * Interface of task.
 * <p>
 * Task is started by an associated model.
 * </p>
 */
public interface ITask {

    void start(Model model);

    boolean isFinished();

    void stop();

}
