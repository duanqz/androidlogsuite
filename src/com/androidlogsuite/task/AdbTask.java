package com.androidlogsuite.task;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.androidlogsuite.model.Model;
import com.androidlogsuite.util.Log;

/*
 * AdbClient: 
 * connect 5037 to setup communication channel with adb server
 * */
public class AdbTask extends Task {
    private static String TAG = AdbTask.class.getName();
    private static int SERVER_PORT = 5037;
    private static String SERVER_IP = "127.0.0.1";
    private static AdbCommandParser mServerResponseParser;

    private static final int PHASE_INITIAL = -1;
    private static final int PHASE_CONNECTED = 0;
    private static final int PHASE_TRANSPORT_SWITCHED = 1;
    private static final int PHASE_CMD_SENT = 2;
    private static final int PHASE_GETTING_RESULTS = 3;
    private static final int PHASE_CMD_FINISHED = 4;
    private static final int PHASE_CMD_ERROR = 5;
    private volatile int mPhase = PHASE_INITIAL;

    private ByteBuffer mpStatusBuffer;

    static {
        mServerResponseParser = new AdbCommandParser();
    }

    static public AdbTask getLogcatTask(String logDevice, boolean printTime) {
        if (logDevice.equals("events") || logDevice.equals("system")
                || logDevice.equals("main")) {
            String cmd = "shell:exec logcat -b " + logDevice;
            if (printTime) {
                cmd += " -v threadtime";
            }
            return new AdbTask(cmd);
        }

        return null;

    }

    public AdbTask() {
        mpStatusBuffer = ByteBuffer
                .wrap(new byte[AdbCommand.ADB_COMMAND_RESPONSE_OK.length()]);
    }

    public AdbTask(String cmd) {
        this();
        mCmd = cmd;
    }


    @Override
    public boolean transmit() {
        switch (mPhase) {
        case PHASE_CMD_ERROR: {
            Log.d(TAG, "Task is in error status");
            return false;
        }
        case PHASE_CMD_FINISHED: {
            Log.d(TAG, "Cmd is finished");
            return false;
        }
        case PHASE_GETTING_RESULTS:// we are getting data
            return true;
        case PHASE_INITIAL: {
            try {

                /*
                 * if (mSocketChannel.isConnectionPending() == false){
                 * Log.d(TAG, "still no connection pending, connect it now");
                 * mSocketChannel.connect(new InetSocketAddress(SERVER_IP,
                 * SERVER_PORT)); }
                 */
                boolean bConnected = mSocketChannel.finishConnect();
                Log.d(TAG, "Task is connected: " + bConnected);
                mPhase = PHASE_CONNECTED;

                if (mCmd.startsWith(AdbCommand.ADB_COMMAND_HOST) == false) {
                    Log.d(TAG, mCmd + " change swith mode");
                    switchTransportTarget();
                    mPhase = PHASE_TRANSPORT_SWITCHED;
                } else {
                    // if no need to switch transport
                    Log.d(TAG, mCmd + " no need to change, send cmd directly");
                    writeCmd(mCmd);
                    mPhase = PHASE_CMD_SENT;
                }
            } catch (Exception e) {
                Log.d(TAG, e);
                e.printStackTrace();
                mPhase = PHASE_CMD_ERROR;
                return false;
            }
        }
            break;
        case PHASE_TRANSPORT_SWITCHED: {
            Log.d(TAG, mCmd + " switch complete,send cmd");
            writeCmd(mCmd);
            mPhase = PHASE_CMD_SENT;
        }
            break;

        default:
            break;
        }

        return true;
    }

    @Override
    public boolean receive() {
        if ((mKey.readyOps() & SelectionKey.OP_READ) != 0) {
            switch (mPhase) {
            case PHASE_CMD_ERROR: {
                Log.d(TAG,
                        "Run Task is in error status, we need close this task");
                return true;
            }
            case PHASE_TRANSPORT_SWITCHED: {
                // handle real command sent
                int status = getStatus();
                if (status == -1)
                    break;
                Log.d(TAG, "switch transport way:" + status);
                if (status == 0)
                    mPhase = PHASE_CMD_ERROR;
                /*
                 * We don't send real command here, because we want
                 * prepareToRun() to send data and run() to get data back.
                 * Howerver, this seems it will cause some problem if another
                 * tasks take too long to handle a data receiving. In that way,
                 * this task even not send its cmd to adbserver.
                 */
            }
                break;
            case PHASE_CMD_SENT: {
                int status = getStatus();
                if (status == -1)
                    break;
                Log.d(TAG, "adbserver handle " + mCmd + ", status = " + status);
                mPhase = status == 1 ? PHASE_GETTING_RESULTS : PHASE_CMD_ERROR;
            }
                break;
            case PHASE_GETTING_RESULTS: {
                if (getResult(mCmd)) {
                    mPhase = PHASE_CMD_FINISHED;
                    return true;
                }

            }
                break;
            }
        }

        return false;
    }

    @Override
    protected String getServerIP() {
        return SERVER_IP;
    }

    @Override
    protected int getServerPort() {
        return SERVER_PORT;
    }

    private String prepareForSend(String cmd) {
        String outString = String.format("%04x",
                new Object[] { new Integer(cmd.length()) })
                + cmd;

        return outString;
    }

    private boolean writeCmd(String cmd) {
        String realCmd = prepareForSend(cmd);
        ByteBuffer outBuffer = ByteBuffer.wrap(realCmd.getBytes());
        return writeToSocket(outBuffer);
    }

    @Override
    public boolean isFinished() {
        return mPhase == PHASE_CMD_FINISHED || mPhase == PHASE_CMD_ERROR;
    }

    // return -1, continue to wait
    // 0:error
    // 1: success
    private int getStatus() {
        try {
            mpStatusBuffer.clear();
            int n = mSocketChannel.read(mpStatusBuffer);
            if (n == 0) {
                Log.d(TAG, "getStatus return 0, wait...");
                return -1;
            }
            String status = new String(mpStatusBuffer.array(), 0,
                    mpStatusBuffer.position());

            Log.d(TAG, "CMD:" + mCmd + " getStatus = " + status);
            return mServerResponseParser.getCmdStatus(status) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean getResult(String cmd) {
        return readFromSocket();
    }

    private boolean switchTransportTarget() {
        return writeCmd(AdbCommand.ADB_COMMAND_TRANSPORT_ANY);
    }

}
