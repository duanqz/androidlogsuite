package com.androidlogsuite.model;

import com.androidlogsuite.configuration.ConfigCenter;
import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.configuration.ParseConfiguration;
import com.androidlogsuite.configuration.ProcessConfiguration;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.task.ITask;
import com.androidlogsuite.util.BufferPool;
import com.androidlogsuite.util.Log;
import com.androidlogsuite.util.ThreadsPool;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class Model {

    ArrayList<ByteBuffer> mCleanBuffers;
    ArrayList<ByteBuffer> mDirtyBuffers;

    protected ModelConfiguration mModelConfig;
    protected ModelParser mModelParser;
    protected ModelListener mModelListener;

    volatile public boolean mbPrintVerbose;
    public int mBufferSize;

    abstract public void draw(Output output);

    abstract public String getModelName();

    abstract public String getAdbCommand();

    public Model(ModelConfiguration modelConfig) {
        mModelConfig = modelConfig;

        if (modelConfig.mBufferSize > 0) {
            mBufferSize = modelConfig.mBufferSize;
        } else {
            mBufferSize = 1 << 10;
        }

        mCleanBuffers = new ArrayList<ByteBuffer>();
        mDirtyBuffers = new ArrayList<ByteBuffer>();

        /*
         * if(this instanceof DynamicModel == false)
         * ModelCenter.getModelCenter().addModel(this);
         */
        mModelListener = ModelCenter.getModelCenter();
    }

    public int getOutputType() {
        return ConfigCenter.getConfigCenter().getOutputType();
    }

    public void setModelListener(ModelListener listener) {
        mModelListener = listener;
    }

    static public interface ModelListener {
        void onModelParsed(Model model);
    }

    public boolean hasOutputData() {
        return mModelParser.hasValidDataForDrawing();
    }

    public void notifyModelParsed() {
        if (mModelListener != null) {
            mModelListener.onModelParsed(this);
        }
    }

    public ProcessConfiguration getProcessConfiguration() {
        return mModelConfig.mProcessConfiguration;
    }

    public ByteBuffer getCleanBuffer() {
        synchronized (mCleanBuffers) {
            if (mCleanBuffers.size() == 0) {
                ByteBuffer newBuffer = BufferPool.getBufferPool()
                        .getByteBuffer(mBufferSize);
                mCleanBuffers.add(newBuffer);
            }
            return mCleanBuffers.get(0);
        }
    }

    /*
     * remove dirty buffer from clean buffer lists and put it into dirty buffer
     * lists if dirtybuffer == null, it is a sign of adb task is closed.
     */
    public void putCleanBuffer(ByteBuffer dirtyBuffer) {
        if (dirtyBuffer != null) {
            synchronized (mCleanBuffers) {
                mCleanBuffers.remove(dirtyBuffer);
            }
        }
        synchronized (mDirtyBuffers) {
            mDirtyBuffers.add(dirtyBuffer);
            try {
                mDirtyBuffers.notifyAll();
            } catch (Exception e) {
            }
        }
        if (mModelParser != null) {
            if (mModelParser.mWorking == true)
                return;
            synchronized (mModelParser) {
                ThreadsPool.getThreadsPool().addTask(mModelParser);
                mModelParser.mWorking = true;
            }

        }
    }

    // if returns null, it is a sign of quit waiting
    public Object[] getDirtyBuffers() {
        synchronized (mDirtyBuffers) {
            if (mDirtyBuffers.size() == 0) {
                try {
                    mDirtyBuffers.wait();
                } catch (Exception e) {
                    Log.d(getModelName(), "getDirtyBuffers quit " + e);
                    return null;
                }
            }
            return mDirtyBuffers.toArray();
        }
    }

    // Only if buffer has all been consumed (position==capacity)
    // we will clear it, otherwise we don't clear the buffer
    public void putDirtyBuffers(Object[] freeBuffers) {
        if (freeBuffers == null) {
            return;// a sign of quit? no, check putCleanBuffer
        }
        synchronized (mDirtyBuffers) {
            for (Object object : freeBuffers) {
                ByteBuffer buffer = (ByteBuffer) object;
                mDirtyBuffers.remove(buffer);
                if (buffer.position() == buffer.capacity())
                    buffer.clear();
            }
        }
        synchronized (mCleanBuffers) {
            for (Object object : freeBuffers) {
                ByteBuffer buffer = (ByteBuffer) object;
                mCleanBuffers.add(buffer);
            }
        }
    }

    public void destroy() {
        synchronized (mDirtyBuffers) {
            while (mDirtyBuffers.size() > 0) {
                ByteBuffer buffer = mDirtyBuffers.remove(0);
                BufferPool.getBufferPool().putByteBuffer(buffer);
                buffer = null;
            }
        }
        synchronized (mCleanBuffers) {
            while (mCleanBuffers.size() > 0) {
                ByteBuffer buffer = mCleanBuffers.remove(0);
                BufferPool.getBufferPool().putByteBuffer(buffer);
                buffer = null;
            }
        }
        if (mModelParser != null)
            mModelParser.destroy();
    }

    public static abstract class ModelParser implements Runnable {
        public volatile boolean mWorking = false;

        private ByteBuffer mLeftOverBuffer;

        public ParseConfiguration mParseConfiguration;
        public boolean mbParsingFinished = false;

        private String getModelName() {
            return getModel().getModelName();
        }

        // return true, means put the model into output thread
        public boolean parseWithoutParseConfiguration(ByteBuffer parsingBuffer,
                int offset) {
            return true;
        }

        // return true, means stop parsing
        public abstract boolean addParsedResult(String[] parsedResults);


        /**
         * @return Whether has valid data for drawing.
         */
        public abstract boolean hasValidDataForDrawing();

        public boolean parse(ByteBuffer parsingBuffer, int offset) {
            try {
                if (mParseConfiguration == null) {
                    mbParsingFinished = parseWithoutParseConfiguration(
                            parsingBuffer, offset);
                    return mbParsingFinished;
                }

                byte[] buffer = parsingBuffer.array();
                int length = parsingBuffer.position() - offset;

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        buffer, offset, length);

                LineNumberReader lineNumberReader = new LineNumberReader(
                        new InputStreamReader(byteArrayInputStream));

                while (mbParsingFinished == false) {
                    String line = lineNumberReader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (getModel().mbPrintVerbose) {
                        Log.d(getModelName(), line);
                    }

                    String[] parseResults = mParseConfiguration
                            .getParseResult(line);

                    if (parseResults != null) {
                        mbParsingFinished = addParsedResult(parseResults);
                    }
                }

                lineNumberReader.close();
                byteArrayInputStream.close();

                return mbParsingFinished;
            } catch (Exception e) {
                Log.d(getModelName(), "parse error with " + e
                        + " not put into output thread");
                e.printStackTrace();
                return false;
            }
        }

        public boolean isWorking() {
            return mWorking;
        }

        public void destroy() {
            if (mLeftOverBuffer != null) {
                mLeftOverBuffer.clear();
                mLeftOverBuffer = null;
            }
        }

        abstract public Model getModel();

        static public int getLastIndexOfNewLine(ByteBuffer buffer) {
            int lastPos = buffer.position();
            if (buffer.hasRemaining() == false)
                lastPos -= 1;
            for (int lastIndexOfNewLine = lastPos; lastIndexOfNewLine >= 0; lastIndexOfNewLine--) {
                char c = (char) buffer.get(lastIndexOfNewLine);
                if (c == '\n') {
                    return lastIndexOfNewLine;
                }
            }
            return 0;
        }

        static public int getFirstIndexOfNewLine(ByteBuffer buffer) {
            int length = buffer.position();
            for (int firstIndexOfNewLine = 0; firstIndexOfNewLine <= length; firstIndexOfNewLine++) {
                char c = (char) buffer.get(firstIndexOfNewLine);
                if (c == '\n') {
                    return firstIndexOfNewLine;
                }
            }
            return -1;
        }

        private void prepareLeftOverBuffer(ByteBuffer buffer, int offset) {
            int lastIndex = getLastIndexOfNewLine(buffer);
            if (lastIndex != buffer.position() - 1) {
                if (mLeftOverBuffer == null) {
                    byte[] leftoverbuffer = new byte[1024];
                    mLeftOverBuffer = ByteBuffer.wrap(leftoverbuffer);
                }
                try {
                    int copyLength = buffer.position() - lastIndex;
                    mLeftOverBuffer.put(buffer.array(), lastIndex, copyLength);
                } catch (Exception e) {
                    Log.d(getModelName(), e);
                }
                buffer.limit(lastIndex + 1);
            }
        }

        public void run() {
            while (ThreadsPool.isThreadsPoolStopped() == false
                    && mbParsingFinished == false) {
                Object[] dirtyBuffers = getModel().getDirtyBuffers();
                if (dirtyBuffers == null) {
                    Log.d(getModelName(), "Model parse work quit");
                    return;
                }
                for (Object object : dirtyBuffers) {
                    int offset = 0;
                    ByteBuffer buffer = (ByteBuffer) object;
                    if (buffer == null) {
                        Log.d(getModelName(), " " + getModel()
                                + " receiving data is finished");

                        // We might notify with mbParsingFinished is false
                        // If all the buffers have no valid data, force it to be true
                        mbParsingFinished = true;
                        getModel().notifyModelParsed();
                        break;
                    }
                    // concate leftoverbuffer with new buffer
                    if (mLeftOverBuffer != null
                            && mLeftOverBuffer.position() != 0) {
                        int firstIndex = getFirstIndexOfNewLine(buffer);
                        mLeftOverBuffer.put(buffer.array(), 0, firstIndex + 1);
                        offset = firstIndex + 1;
                        int leftoverOffset = 0;
                        if (mLeftOverBuffer.get(0) == '\n') {
                            leftoverOffset = 1;
                        }
                        parse(mLeftOverBuffer, leftoverOffset);
                        mLeftOverBuffer.clear();
                        if (mbParsingFinished) {
                            Log.d(getModelName(), " " + getModel()
                                    + " notify parse finished in leftover job");
                            getModel().notifyModelParsed();
                            break;// since parse is finished, no need to do left
                                  // works.
                        }
                    }
                    // parse new buffer, which may become the new leftover
                    // buffer
                    if (mbParsingFinished == false) {
                        prepareLeftOverBuffer(buffer, offset);
                        parse(buffer, offset);
                        if (mbParsingFinished) {
                            Log.d(getModelName(), " " + getModel()
                                    + " notify parse finished in new job");
                            getModel().notifyModelParsed();
                            break;
                        }
                    }
                    buffer.clear();
                }

                getModel().putDirtyBuffers(dirtyBuffers);
            }
        }
    }
}
