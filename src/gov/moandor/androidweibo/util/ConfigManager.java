package gov.moandor.androidweibo.util;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ConfigManager {
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
	private static final String PREFERENCE_VERSION_KEY = "preference_version";
    private static final String THEME = "theme";
    private static final String CURRENT_ACCOUNT_INDEX = "current_account_index";
    private static final String WEIBO_GROUP = "weibo_group";
    private static final String PICTURE_QUALITY = "picture_quality";
    private static final String PICTURE_WIFI_QUALITY = "picture_wifi_quality";
    private static final String ATME_FILTER = "atme_filter";
    private static final String COMMENT_FILTER = "comment_filter";
    private static final String FAST_SCROLL_ENABLED = "fast_scroll_enabled";
    private static final String FONT_SIZE_MODE = "font_size_mode";
    private static final String AVATAR_QUALITY = "avatar_quality";
    private static final String NO_PICTURE_MODE = "no_picture_mode";
    private static final String LOAD_WEIBO_COUNT_MODE = "load_weibo_count_mode";
    private static final String COMMENT_REPOST_LIST_AVATAR_MODE = "comment_repost_list_avatar_mode";
    private static final String NOTIFICATION_ENABLED = "notification_enabled";
    private static final String NOTIFICATION_FREQUENCY = "notification_frequency";
    private static final String NOTIFICATION_MENTION_WEIBO_ENABLED = "notification_mention_weibo_enabled";
    private static final String NOTIFICATION_COMMENT_ENABLED = "notification_comment_enabled";
    private static final String NOTIFICATION_MENTION_COMMENT_ENABLED = "notification_mention_comment_enabled";
    private static final String NOTIFICATION_VIBRATE_ENABLED = "notification_vibrate_enabled";
    private static final String NOTIFICATION_LED_ENABLED = "notification_led_enabled";
    private static final String NOTIFICATION_RINGTONE = "notification_ringtone";
    private static final String WIFI_AUTO_DOWNLOAD_PIC_ENABLED = "wifi_auto_download_pic_enabled";
    private static final String LIST_HW_ACCEL_ENABLED = "list_hw_accel_enabled";
    private static final String PIC_HW_ACCEL_ENABLED = "pic_hw_accel_enabled";
    private static final String SCREEN_ORIENTATION = "screen_orientation";
	
	public static int getAppTheme() {
        return Integer.parseInt(getPreferences().getString(THEME, String.valueOf(THEME_LIGHT)));
    }
	
    public static void setAppTheme(int theme) {
        sTheme = theme;
    }
	
    public static int getCurrentAccountIndex() {
        return getPreferences().getInt(CURRENT_ACCOUNT_INDEX, 0);
    }
	
    public static void setCurrentAccountIndex(int currentAccountIndex) {
        sCurrentAccountIndex = currentAccountIndex;
    }
	
    public static int getWeiboGroup() {
        return getPreferences().getInt(WEIBO_GROUP, 0);
    }
	
    public static void setWeiboGroup(int group) {
        sWeiboGroup = group;
    }
	
    public static int getPictureQuality() {
        return Integer.parseInt(getPreferences().getString(PICTURE_QUALITY, String.valueOf(PICTURE_SMALL)));
    }
	
    public static void setPictureQuality(int pictureQuality) {
        sPictureQuality = pictureQuality;
    }
	
    public static int getPictureWifiQuality() {
        return getPreferences().getInt(PICTURE_WIFI_QUALITY, PICTURE_LARGE);
    }
	
    public static void setPictureWifiQuality(int pictureWifiQuality) {
        sPictureWifiQuality = pictureWifiQuality;
    }
	
    public static int getAtmeFilter() {
        return getPreferences().getInt(ATME_FILTER, 0);
    }
	
    public static void setAtmeFilter(int atmeFilter) {
        sAtmeFilter = atmeFilter;
    }
	
    public static int getCommentFilter() {
        return sharedPreferences.getInt(COMMENT_FILTER, 0);
    }
	
    public static void setCommentFilter(int commentFilter) {
        sCommentFilter = commentFilter;
    }
	
    public static boolean isFastScrollEnabled() {
        return sharedPreferences.getBoolean(FAST_SCROLL_ENABLED, true);
    }
	
    public static synchronized void setFastScrollEnabled(boolean fastScrollEnabled) {
        sFastScrollEnabled = fastScrollEnabled;
    }
	
    public static int getFontSizeMode() {
        return sFontSizeMode;
    }
	
    public static synchronized void setFontSizeMode(int fontSize) {
        sFontSizeMode = fontSize;
    }
	
    public static int getAvatarQuality() {
        return sAvatarQuality;
    }
	
    public static synchronized void setAvatarQuality(int avatarQuality) {
        sAvatarQuality = avatarQuality;
    }
	
    public static boolean isNoPictureMode() {
        return sNoPictureMode;
    }
	
    public static synchronized void setNoPictureMode(boolean noPictureMode) {
        sNoPictureMode = noPictureMode;
    }
	
    public static int getLoadWeiboCountMode() {
        return sLoadWeiboCountMode;
    }
	
    public static synchronized void setLoadWeiboCountMode(int loadWeiboCountMode) {
        sLoadWeiboCountMode = loadWeiboCountMode;
    }
	
    public static int getCommentRepostListAvatarMode() {
        return sCommentRepostListAvatarMode;
    }
	
    public static synchronized void setCommentRepostListAvatarMode(int commentRepostListAvatarMode) {
        sCommentRepostListAvatarMode = commentRepostListAvatarMode;
    }
	
    public static boolean isNotificationEnabled() {
        return sNotificationEnabled;
    }
	
    public static synchronized void setNotificationEnabled(boolean notificationEnabled) {
        sNotificationEnabled = notificationEnabled;
    }
	
    public static int getNotificationFrequency() {
        return sNotificationFrequency;
    }
	
    public static synchronized void setNotificationFrequency(int notificationFrequency) {
        sNotificationFrequency = notificationFrequency;
    }
	
    public static boolean isNotificationMentionWeiboEnabled() {
        return sNotificationMentionWeiboEnabled;
    }
	
    public static synchronized void setNotificationMentionWeiboEnabled(boolean enabled) {
        sNotificationMentionWeiboEnabled = enabled;
    }
	
    public static boolean isNotificationCommentEnabled() {
        return sNotificationCommentEnabled;
    }
	
    public static synchronized void setNotificationCommentEnabled(boolean enabled) {
        sNotificationCommentEnabled = enabled;
    }
	
    public static boolean isNotificationMentionCommentEnabled() {
        return sNotificationMentionCommentEnabled;
    }
	
    public static synchronized void setNotificationMentionCommentEnabled(boolean enabled) {
        sNotificationMentionCommentEnabled = enabled;
    }
	
    public static boolean isNotificationVibrateEnabled() {
        return sNotificationVibrateEnabled;
    }
	
    public static synchronized void setNotificationVibrateEnabled(boolean enabled) {
        sNotificationVibrateEnabled = enabled;
    }
	
    public static boolean isNotificationLedEnabled() {
        return sNotificationLedEnabled;
    }
	
    public static synchronized void setNotificationLedEnabled(boolean enabled) {
        sNotificationLedEnabled = enabled;
    }
	
    public static String getNotificationRingtone() {
        return sNotificationRingtone;
    }
	
    public static synchronized void setNotificationRingtone(String ringtone) {
        sNotificationRingtone = ringtone;
    }
	
    public static boolean isWifiAutoDownloadPicEnabled() {
        return sWifiAutoDownloadPicEnabled;
    }
	
    public static synchronized void setWifiAutoDownloadPicEnabled(boolean enabled) {
        sWifiAutoDownloadPicEnabled = enabled;
    }
	
    public static boolean isListHwAccelEnabled() {
        return sListHwAccelEnabled;
    }
	
    public static synchronized void setListHwAccelEnabled(boolean enabled) {
        sListHwAccelEnabled = enabled;
    }
	
    public static boolean isPicHwAccelEnabled() {
        return sPicHwAccelEnabled;
    }
	
    public static synchronized void setPicHwAccelEnabled(boolean enabled) {
        sPicHwAccelEnabled = enabled;
    }
	
    public static int getScreenOrientation() {
        return sScreenOrientation;
    }
	
    public static synchronized void setScreenOrientation(int orientation) {
        sScreenOrientation = orientation;
    }
	
	private static SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(sInstance);
	}
}
