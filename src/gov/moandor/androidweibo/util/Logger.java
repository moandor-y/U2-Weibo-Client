package gov.moandor.androidweibo.util;

import android.util.Log;

import gov.moandor.androidweibo.BuildConfig;

public class Logger {
    private static final String DEFAULT_TAG = Logger.class.getSimpleName();
    private static final boolean ENABLED = BuildConfig.DEBUG;
    
    public static void logExcpetion(Throwable throwable) {
        if (ENABLED) {
            throwable.printStackTrace();
        }
    }
    
    public static void debug(String message) {
        if (ENABLED) {
            Log.d(DEFAULT_TAG, message);
        }
    }
    
    public static void debug(String tag, String message) {
        if (ENABLED) {
            Log.d(tag, message);
        }
    }
}
