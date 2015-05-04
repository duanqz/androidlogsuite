package com.androidlogsuite.configuration;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.androidlogsuite.configuration.PlotConfiguration.DataFilter;
import com.androidlogsuite.model.ModelCenter;
import com.androidlogsuite.output.Output;

public class ConfigCenter {
    private static final String TAG = "ConfigCenter";

    public static class FileModelConfiguration extends BaseConfiguration {
        public String mFileName;
        private ArrayList<ModelConfiguration> mFileModels = null;

        public FileModelConfiguration() {
            mFileModels = new ArrayList<ModelConfiguration>();
        }

        public FileModelConfiguration Clone() {
           FileModelConfiguration fileModelConfiguration = new FileModelConfiguration();
           Copy(fileModelConfiguration);
           fileModelConfiguration.mFileName = mFileName;
           fileModelConfiguration.mFileModels = new ArrayList<ModelConfiguration>(mFileModels);
           return fileModelConfiguration;
        }
    }

    private static ConfigCenter gConfigCenter = new ConfigCenter();
    public static HashMap<String, BaseConfiguration> mKeyedIdConfigMaps;

    private HashMap<String, ModelConfiguration> mModels = null;
    private ArrayList<ModelConfiguration> mAdbModels = null;
    private ArrayList<FileModelConfiguration> mFileModels = null;

    public int mOutputType = Output.OUTPUT_TYPE_HTML;
    public String mConfigBaseDir = System.getProperty("user.dir") + "/configuration";
    private String mConfigFileDir = "configs";// default dir
    private String CONFIGCENTER_CONFIG = "/configcenter.xml";

    private ConfigCenter() {
        mModels = new HashMap<String, ModelConfiguration>();
        mAdbModels = new ArrayList<ModelConfiguration>();
        mFileModels = new ArrayList<FileModelConfiguration>();
        mKeyedIdConfigMaps = new HashMap<String, BaseConfiguration>();
    }

    public static ConfigCenter getConfigCenter() {
        return gConfigCenter;
    }

    public ArrayList<ModelConfiguration> getAdbModelConfigs() {
        return mAdbModels;
    }

    public ArrayList<FileModelConfiguration> getFileModelConfigs() {
        return mFileModels;
    }

    public boolean setupConfig() {
        String configCenterXML = mConfigBaseDir + CONFIGCENTER_CONFIG;
        File configCenterFile = new File(configCenterXML);
        if (configCenterFile.exists() == false)
            return false;
        parseConfigFile(configCenterFile);

        furthurParseModels(mAdbModels);

         for (int i = 0; i < mFileModels.size(); i++) {
            FileModelConfiguration fileModelConfiguration = mFileModels.get(i);
            if (fileModelConfiguration.mFileModels.size() == 0) {
                mFileModels.remove(i);
                i--;
                continue;// already parsed as we use id to reference.
            }

            furthurParseModels(fileModelConfiguration.mFileModels);
            if (fileModelConfiguration.mFileModels.size() == 0) {
                mFileModels.remove(i);
                i--;
                continue;// already parsed as we use id to reference.
            }
        }

        return true;

    }

    void furthurParseModels(ArrayList<ModelConfiguration> models) {
        for (int j = 0; j < models.size(); j++) {
            mKeyedIdConfigMaps.clear(); // each file has its own id scope
            ModelConfiguration modelConfiguration = models.get(j);
            if (modelConfiguration.mProcessConfiguration != null)
                continue;// already parsed as we use id to reference.
            String processConfigFile = modelConfiguration.mConfigFileName;
            File file = new File(processConfigFile);
            if (file.exists() == false) {
                models.remove(j);
                j--;
                continue;
            }

            ProcessConfiguration newConfig = parseProcessConfigFile(file);
            if (newConfig == null) {
                models.remove(j);
                j--;
                continue;
            }

            modelConfiguration.mProcessConfiguration = newConfig;
        }
    }

    public int getOutputType() {
        return mOutputType;
    }

    private ProcessConfiguration parseProcessConfigFile(File configFile) {
        ProcessConfiguration processConfiguration = new ProcessConfiguration();
        if (processConfiguration.parseConfigFile(configFile))
            return processConfiguration;
        return null;
    }

    private void parseConfigFile(File configFile) {
        FileReader configReader = null;
        mKeyedIdConfigMaps.clear();
        try {
            configReader = new FileReader(configFile);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(configReader);

            int outDepth = parser.getDepth();
            int type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }

                String tagName = parser.getName();
                if (tagName.equals("configuration")) {
                    String baseDir = parser.getAttributeValue(null,
                            "configuration-dir");
                    if (baseDir != null) {
                        mConfigFileDir = baseDir;
                    }
                } else if (tagName.equals("models")) {
                    parseModels(parser);
                } else if (tagName.equals("output-config")) {
                    String value = parser.getAttributeValue(null, "type");
                    if (value != null) {
                        if (value.equals("html")) {
                            mOutputType = Output.OUTPUT_TYPE_HTML;
                        }
                    }

                }
            }
            configReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    private void parseModels(XmlPullParser parser) {
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("adb-models")) {
                    parseAdbModels(parser);
                } else if (tagName.equals("file-models")) {
                    parseFileModels(parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private void parseAdbModels(XmlPullParser parser) {
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("model-config")) {
                    ModelConfiguration modelConfiguration = parseModelConfig(parser);
                    if (modelConfiguration != null) {
                        BaseConfiguration.replaceConfig(mAdbModels, modelConfiguration);    
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private ModelConfiguration parseModelConfig(XmlPullParser parser) {

        ModelConfiguration modelConfiguration = BaseConfiguration
                .getInstanceFromId(ModelConfiguration.class, parser);
        if (modelConfiguration == null)
            return null;

        if (BaseConfiguration
                .overrideForNewInstance(modelConfiguration, parser)) {
            modelConfiguration = modelConfiguration.Clone();
        }
        if (modelConfiguration.mbParsed)
            return modelConfiguration;

        modelConfiguration.mType = parser.getAttributeValue(null, "type");
        modelConfiguration.mCmd = parser.getAttributeValue(null, "cmd");
        String value = parser.getAttributeValue(null, "printtime");
        if (value != null)
            modelConfiguration.mbPrintTime = Boolean.parseBoolean(value);
        value = parser.getAttributeValue(null, "bufsize");
        if (value != null)
            modelConfiguration.mBufferSize = Integer.parseInt(value);
        value = parser.getAttributeValue(null, "debug");
        if (value != null)
            modelConfiguration.mDebug = Boolean.parseBoolean(value);
        value = parser.getAttributeValue(null, "configfile");
        if (value != null) {
            if (value.startsWith("/"))
                modelConfiguration.mConfigFileName = value;
            else
                modelConfiguration.mConfigFileName = mConfigBaseDir + "/" + mConfigFileDir + value;
        }
        modelConfiguration.mbParsed = true;
        return modelConfiguration;

    }

    private FileModelConfiguration parseFileModel(XmlPullParser parser) {
        FileModelConfiguration fileModelConfiguration = null;

        fileModelConfiguration = BaseConfiguration.getInstanceFromId(
                FileModelConfiguration.class, parser);
        if (fileModelConfiguration == null)
            return null;
        if (BaseConfiguration.overrideForNewInstance(fileModelConfiguration,
                parser)) {
            fileModelConfiguration = (FileModelConfiguration)fileModelConfiguration.Clone();
        }
        if (fileModelConfiguration.mbParsed)
            return fileModelConfiguration;

        String value = parser.getAttributeValue(null, "filename");
        if (value == null)
            return null;
        fileModelConfiguration.mFileName = value;
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("model-config")) {
                    ModelConfiguration modelConfiguration = parseModelConfig(parser);
                    if (modelConfiguration != null) {
                        BaseConfiguration.replaceConfig(fileModelConfiguration.mFileModels, modelConfiguration);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileModelConfiguration.mbParsed = true;
        return fileModelConfiguration;
    }
    
    
    
    
    private void parseFileModels(XmlPullParser parser) {
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("file-model")) {
                    FileModelConfiguration fileModelConfiguration = parseFileModel(parser);
                    if (fileModelConfiguration != null) {
                        mFileModels.add(fileModelConfiguration);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

}
