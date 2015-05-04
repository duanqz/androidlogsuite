package com.androidlogsuite.plugin;

public class PluginCenter {

    static private PluginCenter gPluginCenter = new PluginCenter();

    static public PluginCenter getPluginCenter() {
        return gPluginCenter;
    }

}
