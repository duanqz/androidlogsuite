package com.androidlogsuite.service;

import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Provide file reading service.
 * Waiting for clients connecting, launch work thread to interact with clients when connected.
 * <b>Data format from clients must be <red>"ADBCommand@filename"</red></b>, response FAIL if
 * illegal format is received.
 *
 * @author duanqizhi
 */
public class FileReadService implements Runnable, FileReadWorker.WorkFinishedListener {

    private static final String TAG = FileReadService.class.getSimpleName();

    private static final FileReadService gFileReadService = new FileReadService();

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT  = 9527;

    private static Selector mSelector;
    private static ServerSocketChannel mServerSocketChannel;

    private ByteBuffer mBuffer;     // Buffer for reading data from client
    private Map<String, FileReadWorker> mFileReadWorkers;   // Each filename has a related FileReadWorker

    private volatile boolean bInService;

    private FileReadService() {
        mBuffer = ByteBuffer.wrap(new byte[1024]);
        mFileReadWorkers = new HashMap<String, FileReadWorker>();
    }

    public static FileReadService getFileReadService() {
        return gFileReadService;
    }

    public void start() {
        bInService = true;

        createServerSocket();

        ThreadsPool.getThreadsPool().addTask(this);
    }

    private static void createServerSocket() {
        try {
            if (mSelector == null || !mSelector.isOpen()) {
                mSelector = Selector.open();
            }

            if (mServerSocketChannel == null || !mServerSocketChannel.isOpen()) {
                mServerSocketChannel = ServerSocketChannel.open();
                mServerSocketChannel.configureBlocking(false);
                mServerSocketChannel.socket().bind(new InetSocketAddress(SERVER_IP, SERVER_PORT));
                mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
            }

        } catch (IOException e) {
            Log.d(TAG, "Error when creating server socket. " + e.getMessage());
        }
    }

    public void stop() {
        if (!bInService) {
            return;
        }

        bInService = false;
        mBuffer.clear();

        // Stop all the workers
        for (FileReadWorker worker : mFileReadWorkers.values()) {
            worker.stop();
        }

        mFileReadWorkers.clear();

        try {
            mServerSocketChannel.close();
            mSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Mission done. FileReadService is stopped.");
    }

    @Override
    public void run() {
        try {
            while (bInService) {
                mSelector.select();

                handleConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
            bInService = false;
        }
    }

    private void handleConnection() throws IOException {
        Iterator it = mSelector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = (SelectionKey) it.next();
            it.remove();

            if (key.isAcceptable()) {
                registerSocketChannel(mServerSocketChannel.accept());
            } else {
                dispatchSocketChannel((SocketChannel) key.channel());
            }
        }
    }

    /**
     * Register socket channel to selector.
     */
    private void registerSocketChannel(final SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(mSelector, SelectionKey.OP_READ);
    }

    /**
     * Dispatch socket channel to a FileReadWorker which is related with a filename
     */
    private boolean dispatchSocketChannel(final SocketChannel socketChannel) throws IOException {
        mBuffer.clear();
        if (socketChannel.read(mBuffer) <= 0) {
            socketChannel.write(ByteBuffer.wrap("FAIL".getBytes()));
            return false;
        }

        // The format of data from client is like:
        // adbCommand@filename
        String content = new String(mBuffer.array(), 0, mBuffer.position());
        String[] parts = content.split("@");
        if (parts == null || parts.length < 2) {
            Log.d(TAG, "Error, IllegalArgument from client!!!");
            socketChannel.write(ByteBuffer.wrap("FAIL".getBytes()));
            return false;
        }

        String cmd = parts[0], filename = parts[1];
        FileReadWorker worker = getFileReadWork(filename);
        worker.addConnection(new FileReadWorker.Connection(cmd, socketChannel));
        Log.d(TAG, "Dispatch socket channel to FileReadWorker : " + worker);

        return true;
    }

    private FileReadWorker getFileReadWork(String filename) {
        FileReadWorker worker = mFileReadWorkers.get(filename);
        if (worker == null) {
            worker = new FileReadWorker(filename);

            mFileReadWorkers.put(filename, worker);
            worker.setWorkFinishedListener(this);
            worker.start();

            Log.d(TAG, "A new FileReadWorker is at your service : " + worker);
        }

        return worker;
    }

    @Override
    public void onReadFinished(FileReadWorker fileReadWorker) {
        mFileReadWorkers.remove(fileReadWorker.mFileName);
    }
}

