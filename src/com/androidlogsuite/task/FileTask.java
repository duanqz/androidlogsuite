package com.androidlogsuite.task;

import com.androidlogsuite.service.FileReadService;
import com.androidlogsuite.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * File task client
 *
 * @author duanqizhi
 */
public class FileTask extends Task {

    private static String TAG = FileTask.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final int PHASE_INITIAL = -1;
    private static final int PHASE_CONNECTED = 0;
    private static final int PHASE_CMD_SENT = 1;
    private static final int PHASE_FINISHED = 2;
    private static final int PHASE_ERROR = 3;
    private volatile int mPhase = PHASE_INITIAL;

    private String mFileName;

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    @Override
    public boolean isFinished() {
        return mPhase == PHASE_FINISHED || mPhase == PHASE_ERROR;
    }

    @Override
    public boolean transmit() {
        switch (mPhase) {
            case PHASE_ERROR:
                Log.d(TAG, "Task is in error status");
                return false;

            case PHASE_FINISHED:
                Log.d(TAG, "Task finished");
                return false;

            case PHASE_INITIAL:
                try {
                    boolean bConnected = mSocketChannel.finishConnect();

                    if (DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(this).append("is connected to ").append(mSocketChannel.getRemoteAddress())
                                .append(" : ").append(bConnected);
                        Log.d(TAG, sb.toString());
                    }

                    mPhase = bConnected ? PHASE_CONNECTED : PHASE_INITIAL;
                } catch (IOException e) {
                    Log.d(TAG, "");
                    mPhase = PHASE_ERROR;
                    e.printStackTrace();
                }

                // fall through.
                // Do not break here, as we need to send data to the target

            case PHASE_CONNECTED:
                StringBuilder combine = new StringBuilder();
                combine.append(mCmd).append("@").append(mFileName);
                ByteBuffer buffer = ByteBuffer.wrap(combine.toString().getBytes());
                mPhase = writeToSocket(buffer) ? PHASE_CMD_SENT : PHASE_ERROR;
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public boolean receive() {
        switch (mPhase) {
            case PHASE_ERROR:
                Log.d(TAG, "Task is in error status");
                return false;

            case PHASE_CMD_SENT:
                mPhase = readFromSocket() ? PHASE_FINISHED : PHASE_CMD_SENT;
                break;

            default:
                break;
        }

        return true;
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
