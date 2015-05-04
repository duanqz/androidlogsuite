package com.androidlogsuite.output;

import java.util.ArrayList;

public abstract class Output {
    public static int OUTPUT_TYPE_HTML = 1;
    public static int OUTPUT_TYPE_DB = 2;

    protected ArrayList<OutputItem> mOutputItems = new ArrayList<OutputItem>();

    abstract public boolean createOutput();

    abstract public void drawObject(Object chart);

    public void drawObjects(Object[] objects) {
        return;
    }

    abstract public void closeOutput();

    static public Output getOutputByType(int type) {
        if (type == OUTPUT_TYPE_HTML)
            return new OutputHtml();
        return null;
    }

    static public Output getHTMLOutput() {
        return null;
    }

    public void addOutputItem(OutputItem item) {
        synchronized (mOutputItems) {
            mOutputItems.add(item);
        }
    }

    static public class OutputItem {
        public String name;
        public String description;
        public int minWidth;
        public int minHeight;

        public OutputItem(String name, String description, int minWidth,
                int minHeight) {
            this.name = name;
            this.minHeight = minHeight;
            this.minWidth = minWidth;
            this.description = description;
        }
    }

}
