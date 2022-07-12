package com.sample.dimensionapp;

import static android.util.Log.getStackTraceString;

public class Log
{
    public static int v(String tag, String msg)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.VERBOSE, tag, msg) : 0;
    }

    public static int d(String tag, String msg)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.DEBUG, tag, msg) : 0;
    }

    public static int i(String tag, String msg)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.INFO, tag, msg) : 0;
    }

    public static int w(String tag, String msg)
    {
        return println(android.util.Log.WARN, tag, msg);
    }

    public static int e(String tag, String msg)
    {
        return println(android.util.Log.ERROR, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.VERBOSE, tag, msg, tr) : 0;
    }

    public static int d(String tag, String msg, Throwable tr)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.DEBUG, tag, msg, tr) : 0;
    }

    public static int i(String tag, String msg, Throwable tr)
    {
        return (BuildConfig.DEBUG) ? println(android.util.Log.INFO, tag, msg, tr) : 0;
    }

    public static int w(String tag, String msg, Throwable tr)
    {
        return println(android.util.Log.WARN, tag, msg, tr);
    }

    public static int e(String tag, String msg, Throwable tr)
    {
        return println(android.util.Log.ERROR, tag, msg, tr);
    }

    public static int println(int priority, String tag, String msg)
    {
        return android.util.Log.println(priority, tag, msg);
    }

    public static int println(int priority, String tag, String msg, Throwable tr)
    {
        return android.util.Log.println(priority, tag, msg + '\n' + ((BuildConfig.DEBUG) ? getStackTraceString(tr) : tr.getMessage()));
    }
}