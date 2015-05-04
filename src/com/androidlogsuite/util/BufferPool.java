package com.androidlogsuite.util;

import java.nio.ByteBuffer;

public class BufferPool {

    private static final String TAG = BufferPool.class.toString();

    private static final int BUFFER_SIZE = 4 << 10;
    private static volatile int gAllocatedBuffer = 0;
    private static BufferPool gBufferPool = new BufferPool();

    public static BufferPool getBufferPool() {
        return gBufferPool;
    }

    /*
     * To do lists: 1 In order to add concurrency, please use distributed
     * synchronize lock a) allocate n locks array. b) when getByteBuffer,use
     * random function to get index c) use this index to get the right lock d)
     * use this lock to synchronize;
     */
    private BufferPool() {

    }

    public synchronized ByteBuffer getByteBuffer(int nBufferSize) {
        if (nBufferSize > BUFFER_SIZE)
            nBufferSize = BUFFER_SIZE;
        if (nBufferSize <= 0)
            nBufferSize = BUFFER_SIZE;
        byte[] buff = new byte[nBufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(buff);
        ;
        Log.d(TAG, "allocate " + (++gAllocatedBuffer) + " buffers with size="
                + nBufferSize);
        return buffer;
    }

    public synchronized void putByteBuffer(ByteBuffer freedBuffer) {
        if (freedBuffer == null)
            return;
        byte[] buf = freedBuffer.array();
        freedBuffer.clear();
        buf = null;
        freedBuffer = null;
        ;
        Log.d(TAG, "free buffer,rest is: " + (--gAllocatedBuffer) + " buffers");
        return;
    }

}
