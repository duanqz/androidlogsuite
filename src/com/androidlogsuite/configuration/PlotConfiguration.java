package com.androidlogsuite.configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlotConfiguration extends BaseConfiguration {
    public String mName;
    public DataFilter mDataFilter;
    public static ClassGlobalOption mGlobalOption;
    public ClassOption mOption;

    static public class DataFilter extends BaseConfiguration {
        public static final int SORT_ORDER_ASCEND = 0;
        public static final int SORT_ORDER_DESCEND = 1;
        public static final int SORT_ORDER_DEFAULT = SORT_ORDER_DESCEND;

        public int mSortOrder = SORT_ORDER_DEFAULT;
        public int mWaterMark = 0; // unit is MB

        public DataFilter Clone() {
            DataFilter newDataFilter = new DataFilter();
            Copy(newDataFilter);
            newDataFilter.mSortOrder = mSortOrder;
            newDataFilter.mWaterMark = mWaterMark;
            return newDataFilter;
        }
    }

    static public abstract class HChartsBase extends BaseConfiguration {
        protected String mObjectName;
        private static int listIndex = 0;
        private static int objectIndex = 0;

        protected HChartsBase() {
            mObjectName = getObjectName(this);
        }

        public String getObjectName() {
            return mObjectName;
        }

        public static String getObjectName(Object object) {
            if (object instanceof List<?>) {
                return "objectList_" + (listIndex++);
            }
            return "object_" + (objectIndex++);
        }

        public abstract void getJavaScriptDes(StringBuilder jsBuilder);

        public void getLiteralJavaScriptDes(StringBuilder jsBuilder) {
            return;
        }
    }

    public StringBuilder getJavaScriptDes(StringBuilder jsBuilder) {
        if (mOption != null) {
            mOption.getJavaScriptDes(jsBuilder);
        }
        return jsBuilder;
    }

    public String getTitle() {
        if (mOption != null && mOption.title != null)
            return mOption.title.text;
        return null;
    }

    /*
     * public static Color getColorFromInt(int color){ int red, green, blue; red
     * = color >> 16; green = (color & 0x00FF00) >> 8; blue = (color &
     * 0x0000FF); return new Color(red, green, blue); }
     */

    static public class ClassGlobal extends HChartsBase {
        public Date date;
        public String VMLRadialGradientURL;
        public String canvasToolsURL;
        public int timezoneOffset;
        public boolean useUTC;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            // TODO Auto-generated method stub

        }

        public ClassGlobal Clone() {
            ClassGlobal newObject = new ClassGlobal();
            Copy(newObject);
            newObject.date = date;
            newObject.VMLRadialGradientURL = VMLRadialGradientURL;
            newObject.canvasToolsURL = canvasToolsURL;
            newObject.timezoneOffset = timezoneOffset;
            newObject.useUTC = useUTC;
                    
            return newObject;
        }
    }

    static public class ClassLang extends HChartsBase {
        public String contextButtonTitle;
        public String decimalPoint;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            // TODO Auto-generated method stub

        }

        public ClassLang Clone() {
            ClassLang newObject = new ClassLang();
            Copy(newObject);
           
            newObject.contextButtonTitle = contextButtonTitle;
            newObject.decimalPoint = decimalPoint;
            return newObject;

        }
    }

    static public class ClassGlobalOption {

    }

    static public class Classchart extends HChartsBase {
        public boolean alignTicks;
        public boolean animation;
        public int backgroundColor;
        public int borderColor;
        public String renderTo;
        public String type;
        public String plotBackgroundColor;
        public int plotBorderWidth = 1;
        public boolean plotShadow;
        public String zoomType;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (renderTo != null)
                jsBuilder.append(String.format("%s.renderTo='%s';\n",
                        mObjectName, renderTo));
            if (type != null)
                jsBuilder.append(String.format("%s.type='%s';\n", mObjectName,
                        type));
            if (plotBackgroundColor != null)
                jsBuilder.append(String.format(
                        "%s.plotBackgroundColor='%s';\n", mObjectName,
                        plotBackgroundColor));
            if (zoomType != null) {
                jsBuilder.append(String.format("%s.zoomType='%s';\n",
                        mObjectName, zoomType));
            }

            jsBuilder.append(String.format("%s.plotBorderWidth=%s;\n",
                    mObjectName, Integer.toString(plotBorderWidth)));
            jsBuilder.append(String.format("%s.plotShadow=%s;\n", mObjectName,
                    Boolean.toString(plotShadow)));
            jsBuilder.append(String.format("%s.animation=%s;\n", mObjectName,
                    Boolean.toString(animation)));

        }

        public Classchart Clone() {
            Classchart newObject = new Classchart();
            Copy(newObject);
            newObject.animation = animation;
            newObject.alignTicks =alignTicks;
            newObject.backgroundColor = backgroundColor;
            newObject.borderColor = borderColor;
            newObject.renderTo = renderTo;
            newObject.type =type;
            newObject.plotBackgroundColor = plotBackgroundColor;
            newObject.plotBorderWidth =plotBorderWidth;
            newObject.plotShadow = plotShadow;
            newObject.zoomType =zoomType;
            return newObject;
        }
    }

    static public class Classitems extends HChartsBase {
        public String html;
        public Object style;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            // TODO Auto-generated method stub

        }
        public Classitems Clone() {
            Classitems newObject = new Classitems();
            Copy(newObject);
            newObject.html = html;
            newObject.style =style;
            return newObject;
        }
    }

    static public class Classlabels extends HChartsBase {

        public Object style;

        public ArrayList<Classitems> items;
        public boolean enabled;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.enabled=%s;\n", mObjectName,
                    Boolean.toString(enabled)));

        }

        public Classlabels Clone() {
            Classlabels newObject = new Classlabels();
            Copy(newObject);
            newObject.style = style;
            newObject.enabled =enabled;
            if(items != null){
                newObject.items = new ArrayList<Classitems>(items);
            }
                
            return newObject;
        }

    }

    static public class Classtitle extends HChartsBase {
        public String align;
        public boolean floating;
        public String text;
        public String xNumber;
        public String yNumber;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String
                    .format("%s.text='%s';\n", mObjectName, text));
        }

        public Classtitle Clone() {
            Classtitle newObject = new Classtitle();
            Copy(newObject);
            newObject.align = align;
            newObject.floating =floating;
            newObject.text = text;
            newObject.xNumber =xNumber;
            newObject.yNumber =yNumber;
            return newObject;
        }
    }

    static public class ClassAxis extends HChartsBase {
        public boolean allowDecimals;
        public ArrayList<String> categories;
        public String type;
        public Classtitle title;
        public int min = 0;
        public Classlabels labels;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.allowDecimals = %s;\n",
                    mObjectName, Boolean.toString(allowDecimals)));
            if (type != null) {
                jsBuilder.append(String.format("%s.type = '%s';\n",
                        mObjectName, type));
            }
            if (categories != null) {
                String dataName = getObjectName(categories);
                jsBuilder.append(String.format("var %s = new Array();\n",
                        dataName));
                for (int i = 0; i < categories.size(); i++) {
                    String aData = categories.get(i);
                    jsBuilder.append(String.format("%s[%d]='%s';\n", dataName,
                            i, aData));
                }
                jsBuilder.append(String.format("%s.categories=%s;\n",
                        mObjectName, dataName));
            }

            if (title != null) {
                title.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.title=%s;\n", mObjectName,
                        title.getObjectName()));
            }
            if (labels != null) {
                labels.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.labels=%s;\n", mObjectName,
                        labels.getObjectName()));
            }

        }

        public ClassAxis Clone() {
            ClassAxis newObject = new ClassAxis();
            Copy(newObject);
            
            newObject.allowDecimals = allowDecimals;
            if(categories != null)
                newObject.categories = new ArrayList<String>(categories);
            newObject.type = type;
            newObject.title =title;
            newObject.min =min;
            newObject.labels =labels;
            return newObject;
        }

    }

    static public class Classdata extends HChartsBase {
        public Object color;
        public Object dataLabels;
        public String name;
        public String x;
        public String y;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (name != null) {
                jsBuilder.append(String.format("%s.name = '%s';\n",
                        mObjectName, name));
            }
            if (x != null) {
                jsBuilder.append(String.format("%s.x = %s;\n", mObjectName, x));
            }
            if (y != null) {
                jsBuilder.append(String.format("%s.y = %s;\n", mObjectName, y));
            }
        }

        @Override
        public void getLiteralJavaScriptDes(StringBuilder literalJSBuilder) {
            literalJSBuilder.append("{");
            boolean bNeedSpleed = false;
            if (name != null) {
                literalJSBuilder.append(String.format("name:'%s'", name));
                bNeedSpleed = true;
            }
            if (x != null) {
                if (bNeedSpleed)
                    literalJSBuilder.append(",");
                literalJSBuilder.append(String.format("x:%s", x));
                bNeedSpleed = true;
            }
            if (y != null) {
                if (bNeedSpleed)
                    literalJSBuilder.append(",");
                literalJSBuilder.append(String.format("y:%s", y));
                bNeedSpleed = true;
            }
            literalJSBuilder.append("}");
        }

        public Classdata Clone() {
            Classdata newObject = new Classdata();
            Copy(newObject);
            newObject.color = color;
            newObject.color = color;
            newObject.name = name;
            newObject.x = x;
            newObject.y = y;
            return newObject;
        }
    }

    static public class ClassSeries extends HChartsBase {
        public String type;
        public String name;
        public ArrayList<Classdata> data;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (type != null) {
                jsBuilder.append(String.format("%s.type = '%s';\n",
                        mObjectName, type));
            }
            if (name != null) {
                jsBuilder.append(String.format("%s.name = '%s';\n",
                        mObjectName, name));
            }
            // There may be too many data, so use literal way to construct data;
            if (data != null) {
                String dataName = getObjectName(data);
                StringBuilder dataBuilder = new StringBuilder();
                /*
                 * jsBuilder.append(String.format("var %s = new Array();\n",
                 * dataName));
                 */
                for (int i = 0; i < data.size(); i++) {
                    Classdata aData = data.get(i);
                    aData.getLiteralJavaScriptDes(dataBuilder);
                    if (i < data.size() - 1)
                        dataBuilder.append(",");
                    dataBuilder.append("\n");

                    // aData.getJavaScriptDes(jsBuilder);
                    // jsBuilder.append(String.format("%s[%d]=%s;\n", dataName,
                    // i,
                    // aData.getObjectName()));
                }
                jsBuilder.append(String.format("var %s = new Array(%s);\n",
                        dataName, dataBuilder.toString()));
                dataBuilder = null;
                jsBuilder.append(String.format("%s.data=%s;\n", mObjectName,
                        dataName));
            }
        }
        public ClassSeries Clone() {
            ClassSeries newObject = new ClassSeries();
            Copy(newObject);
            newObject.type = type;
            newObject.name = name;
            newObject.name = name;
            if(data != null){
                newObject.data = new ArrayList<Classdata>(data);
            }
            return newObject;
        }
    }

    static public class Classpie extends HChartsBase {
        public boolean allowPointSelect;
        public String cursor;
        public ClassdataLabels datalabels;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.allowPointSelect=%s;\n",
                    mObjectName, Boolean.toString(allowPointSelect)));
            if (cursor != null)
                jsBuilder.append(String.format("%s.cursor='%s';\n",
                        mObjectName, cursor));

            if (datalabels != null) {
                datalabels.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.dataLabels=%s;\n",
                        mObjectName, datalabels.getObjectName()));
            }
        }
        public Classpie Clone() {
            Classpie newObject = new Classpie();
            Copy(newObject);
            newObject.allowPointSelect = allowPointSelect;
            newObject.cursor = cursor;
            newObject.datalabels = datalabels;
            return newObject;
        }

    }

    static public class Classbar extends HChartsBase {
        public boolean allowPointSelect;
        public String cursor;
        public ClassdataLabels datalabels;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.allowPointSelect=%s;\n",
                    mObjectName, Boolean.toString(allowPointSelect)));
            if (cursor != null)
                jsBuilder.append(String.format("%s.cursor='%s';\n",
                        mObjectName, cursor));

            if (datalabels != null) {
                datalabels.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.dataLabels=%s;\n",
                        mObjectName, datalabels.getObjectName()));
            }
        }

        public Classbar Clone() {
            Classbar newObject = new Classbar();
            Copy(newObject);
            newObject.allowPointSelect = allowPointSelect;
            newObject.cursor = cursor;
            newObject.datalabels = datalabels;
            return newObject;
        }
    }

    static public class Classarea extends HChartsBase {
        public boolean allowPointSelect;
        public String cursor;
        public ClassdataLabels datalabels;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.allowPointSelect=%s;\n",
                    mObjectName, Boolean.toString(allowPointSelect)));
            if (cursor != null)
                jsBuilder.append(String.format("%s.cursor='%s';\n",
                        mObjectName, cursor));

            if (datalabels != null) {
                datalabels.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.dataLabels=%s;\n",
                        mObjectName, datalabels.getObjectName()));
            }
        }
        public Classarea Clone() {
            Classarea newObject = new Classarea();
            Copy(newObject);
            newObject.allowPointSelect = allowPointSelect;
            newObject.cursor = cursor;
            newObject.datalabels = datalabels;
            return newObject;
        }
    }

    static public class ClassdataLabels extends HChartsBase {
        public boolean enabled;
        public String format;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            jsBuilder.append(String.format("%s.enabled=%s;\n", mObjectName,
                    Boolean.toString(enabled)));
            if (format != null)
                jsBuilder.append(String.format("%s.format='%s';\n",
                        mObjectName, format));

        }

        public ClassdataLabels Clone() {
            ClassdataLabels newObject = new ClassdataLabels();
            Copy(newObject);
            newObject.enabled = enabled;
            newObject.format = format;
            return newObject;
        }
    }

    static public class Classtooltip extends HChartsBase {
        public String pointFormat;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (pointFormat != null)
                jsBuilder.append(String.format("%s.pointFormat='%s';\n",
                        mObjectName, pointFormat));

        }

     public Classtooltip Clone() {
            Classtooltip newObject = new Classtooltip();
            Copy(newObject);
            newObject.pointFormat = pointFormat;
            return newObject;
        }
    }

    static public class ClassPlotOption extends HChartsBase {
        public Classpie pie;
        public Classbar bar;
        public Classarea area;

        @Override
        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (pie != null) {
                pie.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.pie=%s;\n", mObjectName,
                        pie.getObjectName()));
            }
            if (bar != null) {
                bar.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.bar=%s;\n", mObjectName,
                        bar.getObjectName()));
            }
            if (area != null) {
                area.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.area=%s;\n", mObjectName,
                        area.getObjectName()));
            }
        }
        public ClassPlotOption Clone() {
            ClassPlotOption newObject = new ClassPlotOption();
            Copy(newObject);
            newObject.pie = pie;
            newObject.bar = bar;
            newObject.area = area;
         
            return newObject;
        }
    }

    // we need to fill this object
    static public class ClassOption extends HChartsBase {
        public Classchart chart;
        public Classlabels labels;
        public ArrayList<ClassSeries> series;
        public Classtitle title;
        public ClassAxis xAxis;
        public ClassAxis yAxis;
        public ClassPlotOption plotOption;
        public Classtooltip tooltip;

        public void getJavaScriptDes(StringBuilder jsBuilder) {
            jsBuilder.append(String.format("var %s = new Object();\n",
                    mObjectName));
            if (chart != null) {
                chart.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.chart=%s;\n", mObjectName,
                        chart.getObjectName()));

            }
            if (labels != null) {
                labels.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.labels=%s;\n", mObjectName,
                        labels.getObjectName()));
            }
            if (title != null) {
                title.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.title=%s;\n", mObjectName,
                        title.getObjectName()));
            }
            if (tooltip != null) {
                tooltip.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.tooltip=%s;\n", mObjectName,
                        tooltip.getObjectName()));
            }
            if (plotOption != null) {
                plotOption.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.plotOptions=%s;\n",
                        mObjectName, plotOption.getObjectName()));
            }
            if (series != null) {
                String seriesName = getObjectName(series);
                jsBuilder.append(String.format("var %s = new Array();\n",
                        seriesName));
                for (int i = 0; i < series.size(); i++) {
                    ClassSeries aSeries = series.get(i);
                    aSeries.getJavaScriptDes(jsBuilder);
                    jsBuilder.append(String.format("%s[%d]=%s;\n", seriesName,
                            i, aSeries.getObjectName()));
                }
                jsBuilder.append(String.format("%s.series=%s;\n", mObjectName,
                        seriesName));
            }
            if (xAxis != null) {
                xAxis.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.xAxis=%s;\n", mObjectName,
                        xAxis.getObjectName()));
            }
            if (yAxis != null) {
                yAxis.getJavaScriptDes(jsBuilder);
                jsBuilder.append(String.format("%s.yAxis=%s;\n", mObjectName,
                        yAxis.getObjectName()));
            }
        }

        public ClassOption Clone() {
            ClassOption newObject = new ClassOption();
            Copy(newObject);
            newObject.chart = chart;
            newObject.labels = labels;
            if(series!= null){
                newObject.series = new ArrayList<ClassSeries>(series);
            }
            newObject.title = title;
            newObject.xAxis = xAxis;
            newObject.yAxis = yAxis;
            newObject.plotOption = plotOption;
            newObject.tooltip = tooltip;
            return newObject;
        }
    }

    public PlotConfiguration Clone() {
        PlotConfiguration newObject = new PlotConfiguration();
        Copy(newObject);
        newObject.mName = mName;
        newObject.mDataFilter = mDataFilter;
        newObject.mOption = mOption;
        return newObject;
    }

}
