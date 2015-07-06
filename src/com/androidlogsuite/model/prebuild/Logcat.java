package com.androidlogsuite.model.prebuild;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;

import com.androidlogsuite.configuration.ModelConfiguration;
import com.androidlogsuite.model.Model;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.util.Log;

public class Logcat extends Model {

    private static final String TAG = "Logcat";

    @Override
    public String getAdbCommand() {
        StringBuilder cmd = new StringBuilder();
        cmd.append("shell:exec logcat -b ").append(mModelConfig.mCmd);
        if (mModelConfig.mbPrintTime) {
            cmd.append(" -v threadtime");
        }
        return cmd.toString();
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

    public Logcat(ModelConfiguration modelConfig) {
        super(modelConfig);
        mModelParser = new LogcatModelParser(this);
    }

    static private class LogcatModelParser extends Model.ModelParser {

        private Logcat mModel;

        private boolean bHasValidDataForDrawing;

        public LogcatModelParser(Model model) {
            mModel = (Logcat) model;
        }

        @Override
        public boolean addParsedResult(String[] parsedResults) {
            Log.d(TAG, parsedResults);
            return false;
        }

        @Override
        public boolean hasValidDataForDrawing() {
            return bHasValidDataForDrawing;
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
