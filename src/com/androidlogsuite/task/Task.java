package com.androidlogsuite.task;


import com.androidlogsuite.model.Model;
import com.androidlogsuite.util.Log;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Abstract task to connect the target server by socket
 *
 */
public abstract class Task implements ISocketTask {

    private final boolean DEBUG = true;

    protected static final String TAG = Task.class.getSimpleName();

    protected SelectionKey mKey;
    protected SocketChannel mSocketChannel;

    protected Model mAssociatedModel;   // each task uni-associated a model
    protected String mCmd;              // adb command transmitting to target

    @Override
    public void start(Model model) {
        mAssociatedModel = model;
        mCmd = model.getAdbCommand();

        if (connect()) {

            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Add task ").append(this).append(" to TaskCenter");
                Log.d(TAG, sb.toString());
            }

            TaskCenter.getTaskCenter().addSocketChannel(mKey, this);

        } else {
            Log.d(TAG, "failed to connect to target!!!");
        }
    }

    @Override
    public void stop() {
        if (mAssociatedModel != null) {
            mAssociatedModel.putCleanBuffer(null);
        }
        close();
    }

    @Override
    public boolean connect() {
        try {
            if (mSocketChannel == null) {
                mSocketChannel = SocketChannel.open();
                mSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", 0));
            }
            SocketAddress serverAddress = new InetSocketAddress(
                    getServerIP(),  getServerPort());

            mSocketChannel.configureBlocking(false);
            mKey = mSocketChannel.register(
                    TaskCenter.getTaskCenter().getSelector(),
                    SelectionKey.OP_READ);

            mSocketChannel.connect(serverAddress);
            return true;
        } catch (Exception e) {
            /*
             * 1 if server not started, we need to launch it 2 2 if version is
             * not right, what to do?
             */
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean transmit() {
        throw new RuntimeException("Should be implemented by derived class");
    }

    @Override
    public boolean receive() {
        throw new RuntimeException("Should be implemented by derived class");
    }

    @Override
    public boolean close() {
        try {
            if (mSocketChannel != null) {
                mSocketChannel.close();
                mSocketChannel = null;
            }

            TaskCenter.getTaskCenter().removeSocketChannel(mKey);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Write data to socket.
     * @param outBuffer
     * @return Whether write completed
     */
    protected boolean writeToSocket(ByteBuffer outBuffer) {
        try {
            mSocketChannel.write(outBuffer);
            outBuffer.clear();
            return true;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Read data from socket. All the data will be put into the associated model
     * @return Whether read completed
     */
    protected boolean readFromSocket() {
        if ((mKey.readyOps() & SelectionKey.OP_READ) != 0) {
            ByteBuffer readBuffer = mAssociatedModel.getCleanBuffer();

            boolean bReadCompleted = false;
            while (readBuffer.hasRemaining()) {
                int nread = 0;
                try {
                    nread = mSocketChannel.read(readBuffer);
                    // Log.Debug(TAG, "read from server: " + nread);
                } catch (ClosedChannelException ioe) {
                    nread = 0;
                    bReadCompleted = true;
                    // Log.Debug(TAG, "read completed:" + ioe.getMessage());
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }

                if (nread == -1 || nread == 0) {
                    bReadCompleted = nread == -1 ? true : false;
                    // Log.Debug(TAG, "read completed? " + bReadCompleted);
                    break;
                }
            }

            // read completed or no buffer
            if (bReadCompleted || readBuffer.hasRemaining() == false) {
                mAssociatedModel.putCleanBuffer(readBuffer);
                Log.d(TAG, "Read " + new String(readBuffer.array(), 0, readBuffer.position()));
                return bReadCompleted;
            }
        }

        return false;
    }

    abstract protected String getServerIP();

    abstract protected int getServerPort();
}
