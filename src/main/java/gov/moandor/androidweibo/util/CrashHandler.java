package gov.moandor.androidweibo.util;

import android.content.pm.PackageInfo;
import android.os.Build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler(Thread.UncaughtExceptionHandler defaultHandler) {
        mDefaultHandler = defaultHandler;
    }

    public static void register() {
        Thread.UncaughtExceptionHandler currHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(currHandler));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        Writer stacktrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stacktrace);
        e.printStackTrace(printWriter);
        Date date = new Date();
        String path = getLogPath(date);
        FileWriter fileWriter = null;
        BufferedWriter out = null;
        try {
            fileWriter = new FileWriter(FileUtils.createFile(path));
            out = new BufferedWriter(fileWriter);
            out.write("Android Weibo Uncaught Exception\n");
            PackageInfo packageInfo =
                    GlobalContext.getInstance().getPackageManager().getPackageInfo(
                            GlobalContext.getInstance().getPackageName(), 0);
            out.write("Version: " + packageInfo.versionName + "\n");
            out.write("Android: " + Build.VERSION.RELEASE + "\n");
            out.write("Manufacturer: " + Build.MANUFACTURER + "\n");
            out.write("Model: " + Build.MODEL + "\n");
            out.write("Date: " + date + "\n\n");
            out.write(stacktrace.toString());
            out.flush();
        } catch (Exception another) {
            Logger.logException(another);
        } finally {
            Utilities.closeSilently(out);
            Utilities.closeSilently(fileWriter);
            mDefaultHandler.uncaughtException(thread, e);
        }
    }

    private String getLogPath(Date date) {
        String fileName = "crash_" + date.getTime() + ".log";
        return FileUtils.LOGS + File.separator + fileName;
    }
}
