package com.androidlogsuite.task;

import com.androidlogsuite.model.Model;
import com.androidlogsuite.service.FileReadService;

/**
 * FileClient
 *
 * @author duanqizhi
 */
public class FileTask extends Task {

    private static String TAG = FileTask.class.getSimpleName();

    @Override
    public boolean transmit() {
        return super.transmit();
    }

    @Override
    public boolean receive() {
        return super.receive();
    }

    @Override
    protected String getServerIP() {
        return FileReadService.SERVER_IP;
    }

    @Override
    protected int getServerPort() {
        return FileReadService.SERVER_PORT;
    }

}
