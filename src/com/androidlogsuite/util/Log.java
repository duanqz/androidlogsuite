package com.androidlogsuite.util;

public class Log {

    public static void d(String TAG, String value) {
        System.out.printf("%s:%s\n", new Object[] { TAG, value });
    }

    public static void d(String TAG, Object value) {
        System.out.printf("%s:%s\n", new Object[] { TAG, value });
    }
}
