package gov.moandor.androidweibo.util;
import android.os.Build;
import gov.moandor.androidweibo.activity.SettingsActivityOldApi;
import gov.moandor.androidweibo.activity.SettingsActivity;

public class CompatUtils {
	public static Class<?> getSettingsActivity() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			return SettingsActivityOldApi.class;
		} else {
			return SettingsActivity.class;
		}
	}
}
