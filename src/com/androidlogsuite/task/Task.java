package com.androidlogsuite.task;

import com.androidlogsuite.model.Model;

public abstract class Task {

    abstract public boolean setup(Model model);

    abstract public boolean prepareToRun();

    // return true means Task is finished
    abstract public boolean run();

    // close task.
    abstract public void close();

    abstract public void setTaskFinished();

    abstract public String getCmd();

}
