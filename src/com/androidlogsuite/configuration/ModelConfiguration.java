package com.androidlogsuite.configuration;

public class ModelConfiguration extends BaseConfiguration {
    public String mConfigFileName;
    public String mCmd;
    public String mType;
    public boolean mbPrintTime;
    public int mBufferSize;
    
    public ProcessConfiguration mProcessConfiguration;

    public ModelConfiguration Clone() {
        ModelConfiguration newModelConfiguration = new ModelConfiguration();
        Copy(newModelConfiguration);
        newModelConfiguration.mConfigFileName = mConfigFileName;
        newModelConfiguration.mCmd = mCmd;
        newModelConfiguration.mType = mType;
        newModelConfiguration.mbPrintTime = mbPrintTime;
        newModelConfiguration.mBufferSize = mBufferSize;
        newModelConfiguration.mProcessConfiguration = mProcessConfiguration;
        return newModelConfiguration;
    }
    
    
    
}
