package com.androidlogsuite.configuration;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

public abstract class BaseConfiguration {

    public String id;
    public boolean mDebug;
    public boolean mbCached;
    public boolean mbParsed;

    static public String getId(String fullId) {
        return fullId.substring(1);
    }
    
    public void Copy(BaseConfiguration newObject){
        newObject.mDebug = mDebug;
        newObject.id = id;
    }
    
    static public <T extends BaseConfiguration> void replaceConfig(ArrayList<T> list, T newItem){
        for(int i = 0; i < list.size(); i++){
            String id = list.get(i).id;
            if(id == null) continue;
            if(newItem.id.equals(id)){
                list.remove(i);
                list.add(i, newItem);
                return;
            }
        }
        list.add(newItem);
        return;
    }
    static public <T extends BaseConfiguration> T getInstanceFromId(Class<T> c,
            XmlPullParser parser) {
        try {
            String id = parser.getAttributeValue(null, "id");
            if (id == null)
                return c.newInstance();

            if (id.startsWith("+")) {
                T newItem = c.newInstance();
                id = getId(id);
                ConfigCenter.getConfigCenter().mKeyedIdConfigMaps.put(id,
                        newItem);
                newItem.mbCached = true;
                newItem.id = id;
                return newItem;
            } else if (id.startsWith("@")) {
                T oldItem = (T) ConfigCenter.getConfigCenter().mKeyedIdConfigMaps
                        .get(getId(id));
                return oldItem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static boolean overrideForNewInstance(BaseConfiguration baseConfiguration,
            XmlPullParser parser) {
        String value = parser.getAttributeValue(null, "override");
        if (value != null && value.equals("true")) {
            // this is the cached one, we need a new one to override
            if (baseConfiguration.mbCached) {
                return true;
            }
        }
        return false;

    }

}
