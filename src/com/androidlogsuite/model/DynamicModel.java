package com.androidlogsuite.model;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;

import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.task.Task;
import com.androidlogsuite.util.Log;

public class DynamicModel extends Model {

    private static final String TAG = "DynamicModel";
    private Task mTask;

    @Override
    public Task getTask() {
        return mTask;
    }

    public void setTask(Task task) {
        mTask = task;
        mTask.setup(this);
    }

    @Override
    public void draw(Output output) {

    }

    @Override
    public String getModelName() {
        return toString();
    }

    public String toString() {
        return getModelName();
    }

    public DynamicModel(ModelConfiguration modelConfig) {
        super(modelConfig);
        mModelParser = new DynamicModelParser(this);
        mBufferSize = 1 << 10;
    }

//    public DynamicModel(int buffersize) {
//        this();
//        if (buffersize <= 0)
//            buffersize = 1 << 10;
//        mBufferSize = buffersize;
//    }

//    public DynamicModel(ModelConfiguration modelConfig) {
//        this(modelConfig);
//        mTask = task;
//        mTask.setup(this);
//    }

    static private class DynamicModelParser extends Model.ModelParser {

        private DynamicModel mModel;

        public DynamicModelParser(Model model) {
            mModel = (DynamicModel) model;
        }

        @Override
        public boolean addParsedResult(String[] parsedResults) {
            Log.d(TAG, parsedResults);
            return false;
        }

        public boolean parseWithoutParseConfiguration(ByteBuffer parsingBuffer,
                int offset) {
            try {
                byte[] buffer = parsingBuffer.array();
                int length = parsingBuffer.position() - offset;

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        buffer, offset, length);

                LineNumberReader lineNumberReader = new LineNumberReader(
                        new InputStreamReader(byteArrayInputStream));
                while (mbParsingFinished == false) {
                    String line = lineNumberReader.readLine();
                    if (line == null)
                        break;
                    if (getModel().mbPrintVerbose)
                        Log.d(TAG, line);
                }
                lineNumberReader.close();
                byteArrayInputStream.close();

                return mbParsingFinished;
            } catch (Exception e) {
                Log.d(TAG, "parse error with " + e
                        + " not put into output thread");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public Model getModel() {
            return mModel;
        }

    }
}
