package com.androidlogsuite.configuration;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.androidlogsuite.configuration.ParseConfiguration.ParseRule;
import com.androidlogsuite.configuration.PlotConfiguration.ClassAxis;
import com.androidlogsuite.configuration.PlotConfiguration.ClassOption;
import com.androidlogsuite.configuration.PlotConfiguration.ClassPlotOption;
import com.androidlogsuite.configuration.PlotConfiguration.Classarea;
import com.androidlogsuite.configuration.PlotConfiguration.Classbar;
import com.androidlogsuite.configuration.PlotConfiguration.Classchart;
import com.androidlogsuite.configuration.PlotConfiguration.ClassdataLabels;
import com.androidlogsuite.configuration.PlotConfiguration.Classlabels;
import com.androidlogsuite.configuration.PlotConfiguration.Classpie;
import com.androidlogsuite.configuration.PlotConfiguration.Classtitle;
import com.androidlogsuite.configuration.PlotConfiguration.Classtooltip;
import com.androidlogsuite.configuration.PlotConfiguration.DataFilter;
import com.androidlogsuite.model.ModelCenter;
import com.androidlogsuite.output.Output;
import com.androidlogsuite.util.Log;

public class ProcessConfiguration extends BaseConfiguration {

    private static String TAG = "ProcessConfiguration";

    public ParseConfiguration mParseConfiguration;

    public HashMap<String, PlotConfiguration> mPlotConfigs;

    public ProcessConfiguration() {
        mPlotConfigs = new HashMap<String, PlotConfiguration>();
    }

    public PlotConfiguration getPlotConfiguration(String name) {
        return mPlotConfigs.get(name);
    }

    public ParseConfiguration getParseConfiguration() {
        return mParseConfiguration;
    }

    public boolean parseConfigFile(File configFile) {
        FileReader configReader = null;
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
                if (tagName.equals("parse-config")) {
                    parseParseConfig(parser);
                } else if (tagName.equals("plot-config")) {
                    parsePlotConfig(parser);
                }
            }
            configReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void parsePlotConfig(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, "name");
        if (mPlotConfigs.containsKey(name)) {
            Log.d(TAG, "PlotConfig " + name + " already defined ");
            return;
        }

        PlotConfiguration newPlotConfig = BaseConfiguration.getInstanceFromId(
                PlotConfiguration.class, parser);
        if (newPlotConfig == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(newPlotConfig, parser))
            newPlotConfig = newPlotConfig.Clone();

        if (newPlotConfig.mbParsed)
            return;

        newPlotConfig.mName = name;
        parsePlotConfigContent(newPlotConfig, parser);
        newPlotConfig.mbParsed = true;
        mPlotConfigs.put(name, newPlotConfig);
        return;

    }

    private void parseDataFilter(PlotConfiguration plotConfig,
            XmlPullParser parser) {

        DataFilter dataFilter = BaseConfiguration.getInstanceFromId(
                DataFilter.class, parser);
        if (dataFilter == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(dataFilter, parser))
            dataFilter = dataFilter.Clone();

        if (dataFilter.mbParsed) {
            plotConfig.mDataFilter = dataFilter;
            return;
        }

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("sort-order")) {
                    String value = parser.getAttributeValue(null, "name");
                    if (value.equals("default") || value.equals("descend")) {
                        dataFilter.mSortOrder = DataFilter.SORT_ORDER_DEFAULT;
                    } else if (value.equals("ascend")) {
                        dataFilter.mSortOrder = DataFilter.SORT_ORDER_ASCEND;
                    }
                } else if (tagName.equals("watermark")) {
                    String value = parser.getAttributeValue(null, "name");
                    dataFilter.mWaterMark = Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataFilter.mbParsed = true;
        plotConfig.mDataFilter = dataFilter;
    }

    private void parseOption(PlotConfiguration plotConfig, XmlPullParser parser) {
     

        ClassOption option = BaseConfiguration.getInstanceFromId(
                ClassOption.class, parser);
        if (option == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option, parser))
            option = option.Clone();

        if (option.mbParsed)
            return;

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("chart")) {
                    parseChart(option, parser);
                } else if (tagName.equals("title")) {
                    option.title = parseTitle(parser);
                } else if (tagName.equals("plotOptions")) {
                    parsePlotOptions(option, parser);
                } else if (tagName.equals("xAxis") || tagName.equals("yAxis")) {
                    boolean bXAxis = tagName.equals("xAxis");
                    ClassAxis axis = parseAxis(parser);
                    if (bXAxis)
                        option.xAxis = axis;
                    else
                        option.yAxis = axis;
                } else if (tagName.equals("tooltip")) {
                    Classtooltip tooltip = parseTooltip(parser);
                    option.tooltip = tooltip;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        option.mbParsed = true;
        plotConfig.mOption = option;
    }

    private Classtooltip parseTooltip(XmlPullParser parser) {
        Classtooltip tooltip = BaseConfiguration.getInstanceFromId(
                Classtooltip.class, parser);
        if (tooltip == null)
            return null;

        if (BaseConfiguration.overrideForNewInstance(tooltip, parser))
            tooltip = tooltip.Clone();

        if (tooltip.mbParsed)
            return tooltip;
        tooltip.pointFormat = parser.getAttributeValue(null, "pointFormat");
        tooltip.mbParsed = true;
        return tooltip;
    }

    private ClassAxis parseAxis(XmlPullParser parser) {

        ClassAxis axis = BaseConfiguration.getInstanceFromId(ClassAxis.class,
                parser);
        if (axis == null)
            return null;

        if (BaseConfiguration.overrideForNewInstance(axis, parser))
            axis = axis.Clone();

        if (axis.mbParsed)
            return axis;

        String value = parser.getAttributeValue(null, "min");
        if (value != null)
            axis.min = Integer.valueOf(value);

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("title")) {
                    axis.title = parseTitle(parser);
                }
                if (tagName.equals("labels")) {
                    axis.labels = parseLabels(parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        axis.mbParsed = true;
        return axis;
    }

    private Classtitle parseTitle(XmlPullParser parser) {
        Classtitle title = BaseConfiguration.getInstanceFromId(
                Classtitle.class, parser);
        if (title == null)
            return null;

        if (BaseConfiguration.overrideForNewInstance(title, parser))
            title = title.Clone();

        if (title.mbParsed)
            return title;

        title.text = parser.getAttributeValue(null, "text");
        title.mbParsed = true;
        return title;
    }

    private Classlabels parseLabels(XmlPullParser parser) {
        Classlabels label = null;
        String value = parser.getAttributeValue(null, "enabled");
        if (value != null) {
            label = BaseConfiguration.getInstanceFromId(Classlabels.class,
                    parser);
            if (label == null)
                return null;

            if (BaseConfiguration.overrideForNewInstance(label, parser))
                label =  label.Clone();

            if (label.mbParsed)
                return label;
            label.enabled = Boolean.parseBoolean(value);
            label.mbParsed = true;
        }
        return label;
    }

    private void parsePlotOptions(ClassOption option, XmlPullParser parser) {

        option.plotOption = BaseConfiguration.getInstanceFromId(
                ClassPlotOption.class, parser);
        if (option.plotOption == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option.plotOption, parser))
            option.plotOption = option.plotOption.Clone();

        if (option.plotOption.mbParsed)
            return;

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("pie")) {
                    parsePie(option.plotOption, parser);
                } else if (tagName.equals("bar")) {
                    parseBar(option.plotOption, parser);
                } else if (tagName.equals("area")) {
                    parseArea(option.plotOption, parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        option.plotOption.mbParsed = true;
    }

    private void parseBar(ClassPlotOption option, XmlPullParser parser) {
        option.bar = BaseConfiguration
                .getInstanceFromId(Classbar.class, parser);
        if (option.bar == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option.bar, parser))
            option.bar = option.bar.Clone();

        if (option.bar.mbParsed)
            return;
        String value = parser.getAttributeValue(null, "allowPointSelect");
        option.bar.allowPointSelect = Boolean.valueOf(value);
        option.bar.cursor = parser.getAttributeValue(null, "cursor");
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("dataLabels")) {
                    option.bar.datalabels = parseDataLabels(parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        option.bar.mbParsed = true;
        return;
    }

    private void parseArea(ClassPlotOption option, XmlPullParser parser) {
        option.area = BaseConfiguration.getInstanceFromId(Classarea.class,
                parser);
        if (option.area == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option.area, parser))
            option.area = option.area.Clone();

        if (option.area.mbParsed)
            return;

        String value = parser.getAttributeValue(null, "allowPointSelect");
        option.area.allowPointSelect = Boolean.valueOf(value);
        option.area.cursor = parser.getAttributeValue(null, "cursor");
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("dataLabels")) {
                    option.area.datalabels = parseDataLabels(parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        option.area.mbParsed = true;
        return;
    }

    private void parsePie(ClassPlotOption option, XmlPullParser parser) {

        option.pie = BaseConfiguration
                .getInstanceFromId(Classpie.class, parser);
        if (option.pie == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option.pie, parser))
            option.pie = option.pie.Clone();

        if (option.pie.mbParsed)
            return;
        String value = parser.getAttributeValue(null, "allowPointSelect");
        option.pie.allowPointSelect = Boolean.valueOf(value);
        option.pie.cursor = parser.getAttributeValue(null, "cursor");
        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("dataLabels")) {
                    option.pie.datalabels = parseDataLabels(parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        option.pie.mbParsed = true;
        return;
    }

    private ClassdataLabels parseDataLabels(XmlPullParser parser) {
        ClassdataLabels labels = BaseConfiguration.getInstanceFromId(
                ClassdataLabels.class, parser);
        if (labels == null)
            return null;

        if (BaseConfiguration.overrideForNewInstance(labels, parser))
            labels =  labels.Clone();

        if (labels.mbParsed)
            return labels;
        String value = parser.getAttributeValue(null, "enabled");
        labels.enabled = Boolean.valueOf(value);
        labels.format = parser.getAttributeValue(null, "format");
        labels.mbParsed = true;
        return labels;
    }

    private void parseChart(ClassOption option, XmlPullParser parser) {

        option.chart = BaseConfiguration.getInstanceFromId(Classchart.class,
                parser);
        if (option.chart == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(option.chart, parser))
            option.chart = option.chart.Clone();

        if (option.chart.mbParsed)
            return;

        option.chart.type = parser.getAttributeValue(null, "type");
        option.chart.renderTo = parser.getAttributeValue(null, "renderTo");
        option.chart.zoomType = parser.getAttributeValue(null, "zoomType");
        String value = parser.getAttributeValue(null, "animation");
        if (value != null)
            option.chart.animation = Boolean.valueOf(value);
        option.chart.mbParsed = true;
        return;
    }

    private void parsePlotConfigContent(PlotConfiguration plotConfig,
            XmlPullParser parser) {

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("data-filter")) {
                    parseDataFilter(plotConfig, parser);
                } else if (tagName.equals("option")) {
                    parseOption(plotConfig, parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseParseConfig(XmlPullParser parser) {
        mParseConfiguration = BaseConfiguration.getInstanceFromId(
                ParseConfiguration.class, parser);
        if (mParseConfiguration == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(mParseConfiguration,
                parser))
            mParseConfiguration = mParseConfiguration.Clone();

        if (mParseConfiguration.mbParsed)
            return;

        String value = parser.getAttributeValue(null, "type");
        if (value != null && value.equals("all"))
            mParseConfiguration.mParseType = ParseConfiguration.PARSE_TYPE_ALL;

        final int innerDepth = parser.getDepth();
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("parse-rule")) {
                    parseParseRule(mParseConfiguration, parser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mParseConfiguration.mbParsed = true;
        return;
    }

    private void parseParseRule(ParseConfiguration parseConfig,
            XmlPullParser parser) {
        ParseRule newParseRule = BaseConfiguration.getInstanceFromId(
                ParseRule.class, parser);
        if (newParseRule == null)
            return;

        if (BaseConfiguration.overrideForNewInstance(newParseRule, parser))
            newParseRule = newParseRule.Clone();

        if (newParseRule.mbParsed)
            return;

        newParseRule.startWith = parser.getAttributeValue(null, "startWith");
        newParseRule.regx = parser.getAttributeValue(null, "regx");
        String value = parser.getAttributeValue(null, "casesensitive");
        if (value != null) {
            newParseRule.bCaseSensitive = Boolean.parseBoolean(value);
        }
        value = parser.getAttributeValue(null, "groups");
        if (value != null) {
            String[] groups = value.split(",");
            if (groups.length == 0) {
                newParseRule.groups = new int[1];
                newParseRule.groups[0] = Integer.valueOf(value);
            } else {
                int N = groups.length;
                newParseRule.groups = new int[N];
                for (int i = 0; i < N; i++) {
                    newParseRule.groups[i] = Integer.valueOf(groups[i]);
                }
            }
        }
        newParseRule.mbParsed = true;
        parseConfig.addParseRule(newParseRule);
    }

    public ProcessConfiguration Clone() {
        ProcessConfiguration processConfiguration = new ProcessConfiguration();
        Copy(processConfiguration);
        processConfiguration.mParseConfiguration = mParseConfiguration;
        if(mPlotConfigs != null)
            processConfiguration.mPlotConfigs = new HashMap<String, PlotConfiguration>(mPlotConfigs);
        return processConfiguration;
    }
}
