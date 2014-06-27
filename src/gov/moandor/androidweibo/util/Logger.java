package gov.moandor.androidweibo.util;

import android.util.Log;

import gov.moandor.androidweibo.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
    private static final String DEFAULT_TAG = Logger.class.getSimpleName();
    private static final String DEBUG_LOG_FILE = FileUtils.LOGS + File.separator + "debug.log";
    private static final boolean ENABLED = BuildConfig.DEBUG;
    
    public static void logException(Throwable throwable) {
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
    
    public static void logToFile(String message) {
        if (ENABLED) {
            String path = DEBUG_LOG_FILE;
            File file = new File(path);
            if (!file.exists()) {
                file = FileUtils.createFile(path);
            }
            FileWriter fileWriter = null;
            BufferedWriter out = null;
            try {
                fileWriter = new FileWriter(file, true);
                out = new BufferedWriter(fileWriter);
                out.write("Date: " + new Date() + "\n\n");
                out.write(message);
                out.write("\n\n\n\n");
                out.flush();
            } catch (IOException e) {
                logException(e);
            } finally {
                Utilities.closeSilently(out);
                Utilities.closeSilently(fileWriter);
            }
            
        }
    }
}
