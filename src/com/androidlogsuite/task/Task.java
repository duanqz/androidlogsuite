package com.androidlogsuite.task;


import com.androidlogsuite.model.Model;
import com.androidlogsuite.util.Log;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
                sb.append("connected to target")
                .append("\nAdd task ").append(this).append(" to TaskCenter");
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

    abstract protected String getServerIP();

    abstract protected int getServerPort();
}
