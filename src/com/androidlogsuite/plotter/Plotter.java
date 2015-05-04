package com.androidlogsuite.plotter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import com.androidlogsuite.configuration.PlotConfiguration;
import com.androidlogsuite.configuration.PlotConfiguration.ClassAxis;
import com.androidlogsuite.configuration.PlotConfiguration.ClassOption;
import com.androidlogsuite.configuration.PlotConfiguration.ClassSeries;
import com.androidlogsuite.configuration.PlotConfiguration.Classdata;
import com.androidlogsuite.configuration.PlotConfiguration.DataFilter;

public class Plotter {

    public static final int DATA_TYPE_DEFAULT_CATEGORY = 0;
    public static final int DATA_TYPE_DEFAULT_PIE = 1;

    public static int[] sort(double[] input, int sortType) {
        int[] indis = new int[input.length];
        boolean bAscend = sortType == DataFilter.SORT_ORDER_ASCEND;
        for (int i = 0; i < input.length; i++) {
            indis[i] = findHeads(input, input[i], i, bAscend);
        }
        TreeMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>();
        for (int i = 0; i < indis.length; i++) {
            sortedMap.put(Integer.valueOf(indis[i]), Integer.valueOf(i));
        }
        Collection<Integer> result = sortedMap.values();
        int index = 0;
        for (Integer one : result) {
            indis[index++] = one.intValue();
        }

        return indis;
    }

    public static int[] sort(long[] input, int sortType) {
        int[] indis = new int[input.length];
        boolean bAscend = sortType == DataFilter.SORT_ORDER_ASCEND;
        for (int i = 0; i < input.length; i++) {
            indis[i] = findHeads(input, input[i], i, bAscend);
        }
        TreeMap<Integer, Integer> sortedMap = new TreeMap<Integer, Integer>();
        for (int i = 0; i < indis.length; i++) {
            sortedMap.put(Integer.valueOf(indis[i]), Integer.valueOf(i));
        }
        Collection<Integer> result = sortedMap.values();
        int index = 0;
        for (Integer one : result) {
            indis[index++] = one.intValue();
        }
        return indis;
    }

    public static int findHeads(long[] input, long key, int myIndex,
            boolean bAscend) {
        int index = 0;
        for (int x = 0; x < input.length; x++) {
            if (x == myIndex)
                continue;
            long one = input[x];
            if (bAscend) {
                if (one < key)
                    index++;
            } else {
                if (one > key)
                    index++;
            }
            if (one == key && x > myIndex)
                index++;
        }

        return index;
    }

    public static int findHeads(double[] input, double key, int myIndex,
            boolean bAscend) {
        int index = 0;
        for (int x = 0; x < input.length; x++) {
            if (x == myIndex)
                continue;
            double one = input[x];
            if (bAscend) {
                if (one >= key)
                    index++;
            } else {
                if (one < key)
                    index++;
            }
        }

        return index;
    }

    // for HTML
    final static String DIV_FORMAT = "<div id=\"%s\" style=\"min-width:%dpx;height:%dpx\"></div>\n";
    final static String JS_FORMAT = "$('#%s').highcharts({\n";

    static public String getDivContainer(String id, int width, int height) {
        return String.format(DIV_FORMAT, id, width, height);
    }

    static public StringBuilder createChart(String id,
            PlotConfiguration plotConfiguration) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        plotConfiguration.getJavaScriptDes(sb);
        sb.append(String.format("var %s = new Highcharts.Chart(%s);\n",
                plotConfiguration.mName,
                plotConfiguration.mOption.getObjectName()));
        sb.append("};\n");
        return sb;
    }

    static public void createAxis(PlotConfiguration plotConfiguration,
            ArrayList<String> categories, boolean bXAis) {
        ClassOption option = plotConfiguration.mOption;
        ClassAxis axis = bXAis ? option.xAxis : option.yAxis;
        if (axis == null)
            return;
        axis.categories = new ArrayList<String>();
        axis.categories.addAll(categories);

        return;
    }

    static public void createData(PlotConfiguration plotConfiguration,
            String seriesName, ArrayList<String> name, ArrayList<Double> x,
            ArrayList<Double> y) {
        ClassOption option = plotConfiguration.mOption;
        if (option == null) {
            return;
        }
        int N = 0;
        if (name != null)
            N = name.size();
        else if (x != null)
            N = x.size();
        else if (y != null)
            N = y.size();
        if (N == 0)
            return;

        if (option.series == null) {
            option.series = new ArrayList<PlotConfiguration.ClassSeries>();
        }

        ClassSeries oneSeries = new ClassSeries();
        if (seriesName != null)
            oneSeries.name = seriesName;
        option.series.add(oneSeries);

        oneSeries.data = new ArrayList<PlotConfiguration.Classdata>();

        for (int i = 0; i < N; i++) {
            Classdata oneData = new Classdata();
            if (name != null)
                oneData.name = name.get(i);
            if (x != null)
                oneData.x = String.format("%f", x.get(i).doubleValue());
            if (y != null)
                oneData.y = String.format("%f", y.get(i).doubleValue());
            oneSeries.data.add(oneData);
        }
        if (x != null && option.xAxis != null && option.xAxis.labels != null) {
            option.xAxis.labels.enabled = true;
        } else if (x == null && option.xAxis != null
                && option.xAxis.labels != null) {
            option.xAxis.labels.enabled = false;
        }
    }

    static public void createData(PlotConfiguration plotConfiguration,
            String seriesName, String[] name, double[] x, double[] y) {
        ClassOption option = plotConfiguration.mOption;
        if (option == null) {
            return;
        }
        int N = 0;
        if (name != null)
            N = name.length;
        else if (x != null)
            N = x.length;
        else if (y != null)
            N = y.length;
        if (N == 0)
            return;
        if (option.series == null) {
            option.series = new ArrayList<PlotConfiguration.ClassSeries>();
        }

        ClassSeries oneSeries = new ClassSeries();
        if (seriesName != null)
            oneSeries.name = seriesName;
        option.series.add(oneSeries);

        oneSeries.data = new ArrayList<PlotConfiguration.Classdata>();
        for (int i = 0; i < N; i++) {
            Classdata oneData = new Classdata();
            if (name != null)
                oneData.name = name[i];
            if (x != null)
                oneData.x = String.format("%f", x[i]);
            if (y != null)
                oneData.y = String.format("%f", y[i]);
            oneSeries.data.add(oneData);
        }

        if (x == null && option.xAxis != null && option.xAxis.labels != null) {
            option.xAxis.labels.enabled = false;
        }
    }

}
