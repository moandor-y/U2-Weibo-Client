package gov.moandor.androidweibo.util;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ConfigManager {
	private static final int PREFERENCE_VERSION = 4;
	public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int PICTURE_SMALL = 0;
    public static final int PICTURE_MEDIUM = 1;
    public static final int PICTURE_LARGE = 2;
    public static final int AVATAR_AUTO = 0;
    public static final int AVATAR_SMALL = 1;
    public static final int AVATAR_LARGE = 2;
    public static final int FONT_SIZE_MODE_SMALL = 0;
    public static final int FONT_SIZE_MODE_MEDIUM = 1;
    public static final int FONT_SIZE_MODE_LARGE = 2;
    public static final int COMMENT_REPOST_LIST_AVATAR_AUTO = 0;
    public static final int COMMENT_REPOST_LIST_AVATAR_ENABLED = 1;
    public static final int COMMENT_REPOST_LIST_AVATAR_DISABLED = 2;
    public static final int THREE_MINUTES = 0;
    public static final int FIFTEEN_MINUTES = 1;
    public static final int HALF_HOUR = 2;
    public static final int LOAD_WEIBO_COUNT_FEWER = 25;
    public static final int LOAD_WEIBO_COUNT_MORE = 100;
    public static final int ORIENTATION_USER = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final float FONT_SIZE_SMALL = 10.0F;
    public static final float FONT_SIZE_MEDIUM = 15.0F;
    public static final float FONT_SIZE_LARGE = 20.0F;
	public static final String PREFERENCE_VERSION_KEY = "preference_version";
    public static final String THEME = "theme";
    public static final String CURRENT_ACCOUNT_INDEX = "current_account_index";
    public static final String WEIBO_GROUP = "weibo_group";
    public static final String PICTURE_QUALITY = "picture_quality";
    public static final String PICTURE_WIFI_QUALITY = "picture_wifi_quality";
    public static final String ATME_FILTER = "atme_filter";
    public static final String COMMENT_FILTER = "comment_filter";
    public static final String FAST_SCROLL_ENABLED = "fast_scroll_enabled";
    public static final String FONT_SIZE_MODE = "font_size_mode";
    public static final String AVATAR_QUALITY = "avatar_quality";
    public static final String NO_PICTURE_MODE = "no_picture_mode";
    public static final String LOAD_WEIBO_COUNT_MODE = "load_weibo_count_mode";
    public static final String COMMENT_REPOST_LIST_AVATAR_MODE = "comment_repost_list_avatar_mode";
    public static final String NOTIFICATION_ENABLED = "notification_enabled";
    public static final String NOTIFICATION_FREQUENCY = "notification_frequency";
    public static final String NOTIFICATION_MENTION_WEIBO_ENABLED = "notification_mention_weibo_enabled";
    public static final String NOTIFICATION_COMMENT_ENABLED = "notification_comment_enabled";
    public static final String NOTIFICATION_MENTION_COMMENT_ENABLED = "notification_mention_comment_enabled";
    public static final String NOTIFICATION_VIBRATE_ENABLED = "notification_vibrate_enabled";
    public static final String NOTIFICATION_LED_ENABLED = "notification_led_enabled";
    public static final String NOTIFICATION_RINGTONE = "notification_ringtone";
    public static final String WIFI_AUTO_DOWNLOAD_PIC_ENABLED = "wifi_auto_download_pic_enabled";
    public static final String LIST_HW_ACCEL_ENABLED = "list_hw_accel_enabled";
    public static final String PIC_HW_ACCEL_ENABLED = "pic_hw_accel_enabled";
    public static final String SCREEN_ORIENTATION = "screen_orientation";
	
	static {
		SharedPreferences sharedPreferences = getPreferences();
		if (sharedPreferences.getInt(PREFERENCE_VERSION_KEY, 0) < PREFERENCE_VERSION) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.clear();
			editor.putInt(PREFERENCE_VERSION_KEY, PREFERENCE_VERSION);
			apply(editor);
		}
	}
	
	public static int getAppTheme() {
        return Integer.parseInt(getPreferences().getString(THEME, String.valueOf(THEME_LIGHT)));
    }
	
	public static void setAppTheme(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(THEME, String.valueOf(value));
		apply(editor);
	}
	
    public static int getCurrentAccountIndex() {
        return getPreferences().getInt(CURRENT_ACCOUNT_INDEX, 0);
    }
	
	public static void setCurrentAccountIndex(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(CURRENT_ACCOUNT_INDEX, value);
		apply(editor);
	}
	
    public static int getWeiboGroup() {
        return getPreferences().getInt(WEIBO_GROUP, 0);
    }
	
	public static void setWeiboGroup(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(WEIBO_GROUP, value);
		apply(editor);
	}
	
    public static int getPictureQuality() {
        return Integer.parseInt(getPreferences().getString(PICTURE_QUALITY, String.valueOf(PICTURE_SMALL)));
    }
	
	public static void setPictureQuality(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(PICTURE_QUALITY, String.valueOf(value));
		apply(editor);
	}
	
    public static int getPictureWifiQuality() {
        return Integer.parseInt(getPreferences().getString(PICTURE_WIFI_QUALITY, String.valueOf(PICTURE_LARGE)));
    }
	
	public static void setPictureWifiQuality(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(PICTURE_WIFI_QUALITY, String.valueOf(value));
		apply(editor);
	}
	
    public static int getAtmeFilter() {
        return getPreferences().getInt(ATME_FILTER, 0);
    }
	
	public static void setAtmeFilter(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(ATME_FILTER, value);
		apply(editor);
	}
	
    public static int getCommentFilter() {
        return getPreferences().getInt(COMMENT_FILTER, 0);
    }
	
	public static void setCommentFilter(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(COMMENT_FILTER, value);
		apply(editor);
	}
	
    public static boolean isFastScrollEnabled() {
        return getPreferences().getBoolean(FAST_SCROLL_ENABLED, true);
    }
	
	public static void setFastScrollEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(FAST_SCROLL_ENABLED, value);
		apply(editor);
	}
	
    public static int getFontSizeMode() {
		return Integer.parseInt(getPreferences().getString(FONT_SIZE_MODE, String.valueOf(FONT_SIZE_MODE_MEDIUM)));
    }
	
	public static void setFontSizeMode(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(FONT_SIZE_MODE, String.valueOf(value));
		apply(editor);
	}
	
    public static int getAvatarQuality() {
		return Integer.parseInt(getPreferences().getString(AVATAR_QUALITY, String.valueOf(AVATAR_AUTO)));
    }
	
	public static void setAvatarQuality(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(AVATAR_QUALITY, String.valueOf(value));
		apply(editor);
	}
	
    public static boolean isNoPictureMode() {
        return getPreferences().getBoolean(NO_PICTURE_MODE, false);
    }
	
	public static void setNoPictureMode(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NO_PICTURE_MODE, value);
		apply(editor);
	}
	
    public static int getLoadWeiboCountMode() {
		return Integer.parseInt(getPreferences().getString(LOAD_WEIBO_COUNT_MODE, "0"));
    }
	
	public static void setLoadWeiboCountMode(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(LOAD_WEIBO_COUNT_MODE, String.valueOf(value));
		apply(editor);
	}
	
    public static int getCommentRepostListAvatarMode() {
		return Integer.parseInt(getPreferences().getString(COMMENT_REPOST_LIST_AVATAR_MODE, String.valueOf(COMMENT_REPOST_LIST_AVATAR_AUTO)));
    }
	
	public static void setCommentRepostListAvatarMode(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(COMMENT_REPOST_LIST_AVATAR_MODE, String.valueOf(value));
		apply(editor);
	}
	
    public static boolean isNotificationEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_ENABLED, false);
    }
	
	public static void setNotificationEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_ENABLED, value);
		apply(editor);
	}
	
    public static int getNotificationFrequency() {
		return Integer.parseInt(getPreferences().getString(NOTIFICATION_FREQUENCY, String.valueOf(FIFTEEN_MINUTES)));
    }
	
	public static void setNotificationFrequency(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(NOTIFICATION_FREQUENCY, String.valueOf(value));
		apply(editor);
	}
	
    public static boolean isNotificationMentionWeiboEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_MENTION_WEIBO_ENABLED, true);
    }
	
	public static void setNotificationMentionWeiboEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_MENTION_WEIBO_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isNotificationCommentEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_COMMENT_ENABLED, true);
    }
	
	public static void setNotificationCommentEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_COMMENT_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isNotificationMentionCommentEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_MENTION_COMMENT_ENABLED, true);
    }
	
	public static void setNotificationMentionCommentEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_MENTION_COMMENT_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isNotificationVibrateEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_VIBRATE_ENABLED, true);
    }
	
	public static void setNotificationVibrateEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_VIBRATE_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isNotificationLedEnabled() {
        return getPreferences().getBoolean(NOTIFICATION_LED_ENABLED, true);
    }
	
	public static void setNotificationLedEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(NOTIFICATION_LED_ENABLED, value);
		apply(editor);
	}
	
    public static String getNotificationRingtone() {
        return getPreferences().getString(NOTIFICATION_RINGTONE, null);
    }
	
	public static void setNotificationRingtone(String value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putString(NOTIFICATION_RINGTONE, value);
		apply(editor);
	}
	
    public static boolean isWifiAutoDownloadPicEnabled() {
        return getPreferences().getBoolean(WIFI_AUTO_DOWNLOAD_PIC_ENABLED, true);
    }
	
	public static void setWifiAutoDownloadPicEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(WIFI_AUTO_DOWNLOAD_PIC_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isListHwAccelEnabled() {
        return getPreferences().getBoolean(LIST_HW_ACCEL_ENABLED, true);
    }
	
	public static void setListHwAccelEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(LIST_HW_ACCEL_ENABLED, value);
		apply(editor);
	}
	
    public static boolean isPicHwAccelEnabled() {
        return getPreferences().getBoolean(PIC_HW_ACCEL_ENABLED, true);
    }
	
	public static void setPicHwAccelEnabled(boolean value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(PIC_HW_ACCEL_ENABLED, value);
		apply(editor);
	}
	
    public static int getScreenOrientation() {
        return getPreferences().getInt(SCREEN_ORIENTATION, ORIENTATION_USER);
    }
	
	public static void setScreenOrientation(int value) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(SCREEN_ORIENTATION, value);
		apply(editor);
	}
	
	private static SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(GlobalContext.getInstance());
	}
	
	private static void apply(SharedPreferences.Editor editor) {
		editor.apply();//TODO api level
	}
}
