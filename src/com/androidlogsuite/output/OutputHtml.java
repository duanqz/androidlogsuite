package com.androidlogsuite.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.androidlogsuite.util.Log;

public class OutputHtml extends Output {
    private static final String TAG = "OutputHTML";
    private String LOG_REPORT_DIR = System.getProperty("user.dir") + "/html";

    private String LOG_REPORT_FILE = LOG_REPORT_DIR + "/logreport.html";
    private File[] mLogFiles = new File[LOGFILE_COUNT];
    private FileOutputStream[] mLogFileOutputStream = new FileOutputStream[LOGFILE_COUNT];

    private static final int LOGFILE_FINAL_HTML = 0;
    private static final int LOGFILE_TMP_JAVASCRIPT = 1;
    private static final int LOGFILE_COUNT = 2;

    public OutputHtml() {
        prepareFiles();
    }

    private String JS_SCROLL_FUNCTION = "window.onscroll=function(){\n"
            + "var headerbottom=document.getElementById(\"main\").offsetTop;\n"
            + "var netBottom=document.body.scrollTop + 10;\n"
            + "if (netBottom > headerbottom) {\n"
            + " document.getElementById(\"sidebar\").style.top=netBottom;\n"
            + "} else {\n"
            + "document.getElementById(\"sidebar\").style.top=headerbottom;\n"
            + "}\n" + "};\n";

    private String CSS_DES = "h1 { color:#333333; }\n"
            + "h3 { color:#333333; }\n"
            + "body { min-width: 800px; max-width:1400px; margin:0 auto; padding:5px; }\n"
            + "#header { text-align:center; margin-top: 30px; }\n"
            + "#sidebar { width: 200px; float:left; background:#ffffff; border: #c0c0c0 1px solid; position:absolute; }\n"
            + "#sidebar ul { list-style:none; margin:0;padding:1px; }\n"
            + "#sidebar li { font-size:12px; color:#333333; margin:0; white-space:nowrap; height: 18px; padding-top:4px; padding-left:4px; }\n"
            + "#sidebar li:hover { background:#7cb5ec; color:#fff; }\n"
            + "#sidebar a { color:#333333; text-decoration:none; }\n"
            + " #content { margin-left: 205px; min-width:700px; }\n";

    private void prepareFiles() {
        try {
            File logDir = new File(LOG_REPORT_DIR);
            if (logDir.exists() == false) {
                logDir.mkdirs();
            }
            deleteTmpFiles(logDir);
            File finalLogFile = new File(LOG_REPORT_FILE);
            if (finalLogFile.exists() == false) {
                finalLogFile.createNewFile();
            }
            mLogFiles[LOGFILE_FINAL_HTML] = finalLogFile;

            mLogFiles[LOGFILE_TMP_JAVASCRIPT] = File.createTempFile(
                    ".logreport-", ".js", logDir);
            mLogFiles[LOGFILE_TMP_JAVASCRIPT].deleteOnExit();

        } catch (Exception e) {
            Log.d(TAG, "prepareFiles fails " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean createOutput() {
        try {
            for (int i = 0; i < LOGFILE_COUNT; i++) {
                mLogFileOutputStream[i] = new FileOutputStream(mLogFiles[i]);
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "createOutput fail " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void writeHTMLHead() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<script src=\"http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.7.2.min.js\"></script>\n");
            sb.append("<script src=\"http://code.highcharts.com/highcharts.js\"></script>\n");

            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(sb.toString()
                    .getBytes());

            // write javascript
            writeJSHead();

            // copy javascript into final html file
            FileInputStream jsInputStream = new FileInputStream(
                    mLogFiles[LOGFILE_TMP_JAVASCRIPT]);
            copyFileToFile(jsInputStream,
                    mLogFileOutputStream[LOGFILE_FINAL_HTML]);
            jsInputStream.close();

            writeJSEnd();

            // write css
            writeCss();

            // write html head end
            writeHTMLHeadEnd();

            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();

        } catch (Exception e) {
            Log.d(TAG, "writeHTMLHead fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeHTMLHeadEnd() {
        try {
            String scriptBegin = "</head>\n";
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(scriptBegin
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();
        } catch (Exception e) {
            Log.d(TAG, "writeHTMLHeadEnd fail " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void writeHTMLBodyEnd() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<body>\n");
            sb.append("<div id=\"header\"><H1>Android Log Report</h1><h3>made by Innost & duanqz</h3><hr/></div>\n");
            sb.append("<div id=\"main\">\n");
            sb.append("<div id=\"sidebar\">\n");
            sb.append("<ul>\n");
            for (int i = 0; i < mOutputItems.size(); i++) {
                OutputItem item = mOutputItems.get(i);
                sb.append(String.format(
                        "<li><a href=\"#%s\"><div>%s</div></a></li>\n",
                        item.name, item.description));
            }
            sb.append("</ul>\n");
            sb.append("</div>\n");
            sb.append("<div id=\"content\">\n");
            for (int i = 0; i < mOutputItems.size(); i++) {
                OutputItem item = mOutputItems.get(i);
                sb.append(String.format("<div id=\"%s\"></div>\n", item.name));
            }

            sb.append("</div>\n");
            sb.append("</div>\n");
            sb.append("</body>\n");
            sb.append("</html>\n");
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(sb.toString()
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();

        } catch (Exception e) {
            Log.d(TAG, "writeHTMLEnd fail " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void writeJSHead() {
        try {
            String scriptBegin = "<script>$(document).ready(function() {\n";
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(scriptBegin
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();
        } catch (Exception e) {
            Log.d(TAG, "writeJSHead fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeJSEnd() {
        try {
            String scriptBegin = "});\n";
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(scriptBegin
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(JS_SCROLL_FUNCTION
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write("</script>\n"
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();
        } catch (Exception e) {
            Log.d(TAG, "writeJSEnd fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void drawObject(Object object) {
        // TODO Auto-generated method stub

    }

    // objects[0] is OutputItem
    // objects[1] is javascript string,need to write to javascript tmp file
    public void drawObjects(Object[] objects) {
        assert (objects.length == 2);
        try {
            addOutputItem((OutputItem) objects[0]);
            mLogFileOutputStream[LOGFILE_TMP_JAVASCRIPT]
                    .write(((String) objects[1]).getBytes());
        } catch (Exception e) {
            Log.d(TAG, "drawObjects fail " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void copyFileToFile(FileInputStream inputStream,
            FileOutputStream fileOutputStream) {
        try {
            byte[] buffer = new byte[1 << 10];
            while (true) {
                int read = inputStream.read(buffer);
                if (read == 0 || read == -1)
                    break;
                fileOutputStream.write(buffer, 0, read);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void closeFiles() {
        try {
            for (int i = 0; i < LOGFILE_COUNT; i++) {
                mLogFileOutputStream[i].close();
                if (i > LOGFILE_FINAL_HTML)
                    mLogFiles[i].delete();
            }
        } catch (Exception e) {
            Log.d(TAG, "closeFiles fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteTmpFiles(File dir) {
        String[] files = dir.list();
        for (String file : files) {
            if (file.startsWith(".logreport-")) {
                File configFile = new File(dir.getAbsolutePath() + "/" + file);
                configFile.delete();
            }
        }
    }

    private void writeCss() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<style type=\"text/css\">\n");
            sb.append(CSS_DES);
            for (int i = 0; i < mOutputItems.size(); i++) {
                OutputItem item = mOutputItems.get(i);
                sb.append(String.format(
                        "#%s {min-width:%dpx;min-height:%dpx;}\n", item.name,
                        item.minWidth, item.minHeight));
            }
            sb.append("</style>\n");
            mLogFileOutputStream[LOGFILE_FINAL_HTML].write(sb.toString()
                    .getBytes());
            mLogFileOutputStream[LOGFILE_FINAL_HTML].flush();
        } catch (Exception e) {
            Log.d(TAG, "writeJSEnd fail " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void closeOutput() {
        try {
            mLogFileOutputStream[LOGFILE_TMP_JAVASCRIPT].flush();

            // write html head
            writeHTMLHead();

            // write html body and end of the whole html
            writeHTMLBodyEnd();

            closeFiles();

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}
