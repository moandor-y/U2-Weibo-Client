package gov.moandor.androidweibo.util;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import gov.moandor.androidweibo.activity.SettingsActivity;
import gov.moandor.androidweibo.activity.SettingsActivityOldApi;

public class CompatUtils {
    public static Class<?> getSettingsActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return SettingsActivityOldApi.class;
        } else {
            return SettingsActivity.class;
        }
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void applySharedPreferences(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }
}
