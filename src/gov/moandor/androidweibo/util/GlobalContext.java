package gov.moandor.androidweibo.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.AbsActivity;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalContext extends Application {
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
    private static final int MIN_CACHE_SIZE = 1024 * 1024 * 8;
	private static final int PREFERENCE_VERSION = 2;
    
    private static volatile GlobalContext sInstance;
    private static volatile AbsActivity sActivity;
    private static Handler sHandler;
    private static volatile boolean sFastScrollEnabled;
    private static volatile boolean sNoPictureMode;
    private static volatile boolean sNotificationEnabled;
    private static volatile boolean sNotificationMentionWeiboEnabled;
    private static volatile boolean sNotificationCommentEnabled;
    private static volatile boolean sNotificationMentionCommentEnabled;
    private static volatile boolean sNotificationVibrateEnabled;
    private static volatile boolean sNotificationLedEnabled;
    private static volatile boolean sWifiAutoDownloadPicEnabled;
    private static volatile boolean sListHwAccelEnabled;
    private static volatile boolean sPicHwAccelEnabled;
    private static volatile int sTheme;
    private static volatile int sCurrentAccountIndex;
    private static volatile int sWeiboGroup;
    private static volatile int sPictureQuality;
    private static volatile int sPictureWifiQuality;
    private static volatile int sAtmeFilter;
    private static volatile int sCommentFilter;
    private static volatile int sFontSizeMode;
    private static volatile int sAvatarQuality;
    private static volatile int sLoadWeiboCountMode;
    private static volatile int sCommentRepostListAvatarMode;
    private static volatile int sNotificationFrequency;
    private static volatile int sScreenOrientation;
    private static volatile String sNotificationRingtone;
    private static volatile List<Account> sAccounts;
    private static volatile LruCache<String, Bitmap> sBitmapCache;
    
    private static final Map<String, Bitmap> sEmotionMap = new LinkedHashMap<String, Bitmap>();
    private static final Map<String, String> sWeiboEmotionNameMap = new LinkedHashMap<String, String>();
    static {
        sWeiboEmotionNameMap.put("[爱你]", "e1.png");
        sWeiboEmotionNameMap.put("[抱抱]", "e2.png");
        sWeiboEmotionNameMap.put("[悲伤]", "e3.png");
        sWeiboEmotionNameMap.put("[鄙视]", "e4.png");
        sWeiboEmotionNameMap.put("[闭嘴]", "e5.png");
        sWeiboEmotionNameMap.put("[馋嘴]", "e6.png");
        sWeiboEmotionNameMap.put("[吃惊]", "e7.png");
        sWeiboEmotionNameMap.put("[打哈欠]", "e8.png");
        sWeiboEmotionNameMap.put("[鼓掌]", "e9.png");
        sWeiboEmotionNameMap.put("[哈哈]", "e10.png");
        sWeiboEmotionNameMap.put("[害羞]", "e11.png");
        sWeiboEmotionNameMap.put("[汗]", "e12.png");
        sWeiboEmotionNameMap.put("[呵呵]", "e13.png");
        sWeiboEmotionNameMap.put("[黑线]", "e14.png");
        sWeiboEmotionNameMap.put("[哼]", "e15.png");
        sWeiboEmotionNameMap.put("[可爱]", "e16.png");
        sWeiboEmotionNameMap.put("[可怜]", "e17.png");
        sWeiboEmotionNameMap.put("[挖鼻屎]", "e18.png");
        sWeiboEmotionNameMap.put("[泪]", "e19.png");
        sWeiboEmotionNameMap.put("[酷]", "e20.png");
        sWeiboEmotionNameMap.put("[懒得理你]", "e21.png");
        sWeiboEmotionNameMap.put("[钱]", "e22.png");
        sWeiboEmotionNameMap.put("[亲亲]", "e23.png");
        sWeiboEmotionNameMap.put("[花心]", "e24.png");
        sWeiboEmotionNameMap.put("[失望]", "e25.png");
        sWeiboEmotionNameMap.put("[书呆子]", "e26.png");
        sWeiboEmotionNameMap.put("[衰]", "e27.png");
        sWeiboEmotionNameMap.put("[睡觉]", "e28.png");
        sWeiboEmotionNameMap.put("[偷笑]", "e29.png");
        sWeiboEmotionNameMap.put("[吐]", "e30.png");
        sWeiboEmotionNameMap.put("[委屈]", "e31.png");
        sWeiboEmotionNameMap.put("[嘻嘻]", "e32.png");
        sWeiboEmotionNameMap.put("[嘘]", "e33.png");
        sWeiboEmotionNameMap.put("[疑问]", "e34.png");
        sWeiboEmotionNameMap.put("[阴险]", "e35.png");
        sWeiboEmotionNameMap.put("[右哼哼]", "e36.png");
        sWeiboEmotionNameMap.put("[左哼哼]", "e37.png");
        sWeiboEmotionNameMap.put("[晕]", "e38.png");
        sWeiboEmotionNameMap.put("[抓狂]", "e39.png");
        sWeiboEmotionNameMap.put("[怒]", "e40.png");
        sWeiboEmotionNameMap.put("[拜拜]", "e41.png");
        sWeiboEmotionNameMap.put("[思考]", "e42.png");
        sWeiboEmotionNameMap.put("[怒骂]", "e43.png");
        sWeiboEmotionNameMap.put("[囧]", "e44.png");
        sWeiboEmotionNameMap.put("[困]", "e45.png");
        sWeiboEmotionNameMap.put("[愤怒]", "e46.png");
        sWeiboEmotionNameMap.put("[感冒]", "e47.png");
        sWeiboEmotionNameMap.put("[生病]", "e48.png");
        sWeiboEmotionNameMap.put("[挤眼]", "e49.png");
        sWeiboEmotionNameMap.put("[奥特曼]", "e50.png");
        sWeiboEmotionNameMap.put("[good]", "e51.png");
        sWeiboEmotionNameMap.put("[弱]", "e52.png");
        sWeiboEmotionNameMap.put("[ok]", "e53.png");
        sWeiboEmotionNameMap.put("[耶]", "e54.png");
        sWeiboEmotionNameMap.put("[来]", "e55.png");
        sWeiboEmotionNameMap.put("[不要]", "e56.png");
        sWeiboEmotionNameMap.put("[赞]", "e57.png");
        sWeiboEmotionNameMap.put("[熊猫]", "e58.png");
        sWeiboEmotionNameMap.put("[兔子]", "e59.png");
        sWeiboEmotionNameMap.put("[猪头]", "e60.png");
        sWeiboEmotionNameMap.put("[心]", "e62.png");
        sWeiboEmotionNameMap.put("[伤心]", "e63.png");
        sWeiboEmotionNameMap.put("[蜡烛]", "e64.png");
        sWeiboEmotionNameMap.put("[威武]", "e65.png");
        sWeiboEmotionNameMap.put("[蛋糕]", "e66.png");
        sWeiboEmotionNameMap.put("[礼物]", "e67.png");
        sWeiboEmotionNameMap.put("[围观]", "e68.png");
        sWeiboEmotionNameMap.put("[钟]", "e69.png");
        sWeiboEmotionNameMap.put("[太阳]", "e70.png");
        sWeiboEmotionNameMap.put("[月亮]", "e71.png");
        sWeiboEmotionNameMap.put("[右边亮了]", "e72.png");
        sWeiboEmotionNameMap.put("[得意地笑]", "e73.png");
        sWeiboEmotionNameMap.put("[求关注]", "e74.png");
        sWeiboEmotionNameMap.put("[偷乐]", "e75.png");
        sWeiboEmotionNameMap.put("[笑哈哈]", "e76.png");
        sWeiboEmotionNameMap.put("[转发]", "e77.png");
        sWeiboEmotionNameMap.put("[浮云]", "e78.png");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        sHandler = new Handler();
        CrashHandler.register();
        readPreferences();
        switch (sTheme) {
        case THEME_LIGHT:
            setTheme(R.style.Theme_Weibo_Light);
            break;
        case THEME_DARK:
            setTheme(R.style.Theme_Weibo_Dark);
            break;
        }
        sAccounts = new CopyOnWriteArrayList<Account>(DatabaseUtils.getAccounts());
        HttpUtils.trustAllHosts();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        buildEmotionMap();
        buildCache();
        Thread thread = new Thread(new ClearCacheRunnable(), "ClearCacheTask");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        if (Utilities.isBmEnabled()) {
            MyAsyncTask.execute(new UpdateFollowingIdsRunnable());
        }
    }
    
    private void buildCache() {
        int memoryClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = Math.max(MIN_CACHE_SIZE, 1024 * 1024 * memoryClass / 5);
        sBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }
	
	public static synchronized void readPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences();
		if (sharedPreferences.getInt(PREFERENCE_VERSION_KEY, 0) < PREFERENCE_VERSION) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.clear();
			editor.putInt(PREFERENCE_VERSION_KEY, PREFERENCE_VERSION);
			editor.commit();
		}
        sTheme = Integer.parseInt(sharedPreferences.getString(THEME, String.valueOf(THEME_LIGHT)));
        sCurrentAccountIndex = sharedPreferences.getInt(CURRENT_ACCOUNT_INDEX, 0);
        sWeiboGroup = sharedPreferences.getInt(WEIBO_GROUP, 0);
        sPictureQuality = sharedPreferences.getInt(PICTURE_QUALITY, PICTURE_SMALL);
        sPictureWifiQuality = sharedPreferences.getInt(PICTURE_WIFI_QUALITY, PICTURE_LARGE);
        sAtmeFilter = sharedPreferences.getInt(ATME_FILTER, 0);
        sCommentFilter = sharedPreferences.getInt(COMMENT_FILTER, 0);
        sFastScrollEnabled = sharedPreferences.getBoolean(FAST_SCROLL_ENABLED, true);
        sFontSizeMode = sharedPreferences.getInt(FONT_SIZE_MODE, FONT_SIZE_MODE_MEDIUM);
        sAvatarQuality = sharedPreferences.getInt(AVATAR_QUALITY, AVATAR_AUTO);
        sNoPictureMode = sharedPreferences.getBoolean(NO_PICTURE_MODE, false);
        sLoadWeiboCountMode = sharedPreferences.getInt(LOAD_WEIBO_COUNT_MODE, 0);
        sCommentRepostListAvatarMode =
			sharedPreferences.getInt(COMMENT_REPOST_LIST_AVATAR_MODE, COMMENT_REPOST_LIST_AVATAR_AUTO);
        sNotificationEnabled = sharedPreferences.getBoolean(NOTIFICATION_ENABLED, false);
        sNotificationFrequency = sharedPreferences.getInt(NOTIFICATION_FREQUENCY, FIFTEEN_MINUTES);
        sNotificationMentionWeiboEnabled = sharedPreferences.getBoolean(NOTIFICATION_MENTION_WEIBO_ENABLED, true);
        sNotificationCommentEnabled = sharedPreferences.getBoolean(NOTIFICATION_COMMENT_ENABLED, true);
        sNotificationMentionCommentEnabled = sharedPreferences.getBoolean(NOTIFICATION_MENTION_COMMENT_ENABLED, true);
        sNotificationVibrateEnabled = sharedPreferences.getBoolean(NOTIFICATION_VIBRATE_ENABLED, true);
        sNotificationLedEnabled = sharedPreferences.getBoolean(NOTIFICATION_LED_ENABLED, true);
        sNotificationRingtone = sharedPreferences.getString(NOTIFICATION_RINGTONE, null);
        sWifiAutoDownloadPicEnabled = sharedPreferences.getBoolean(WIFI_AUTO_DOWNLOAD_PIC_ENABLED, true);
        sListHwAccelEnabled = sharedPreferences.getBoolean(LIST_HW_ACCEL_ENABLED, true);
        sPicHwAccelEnabled = sharedPreferences.getBoolean(PIC_HW_ACCEL_ENABLED, true);
        sScreenOrientation = sharedPreferences.getInt(SCREEN_ORIENTATION, ORIENTATION_USER);
		savePreferences();
	}
    
    public static synchronized void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(THEME, String.valueOf(sTheme));
        editor.putInt(CURRENT_ACCOUNT_INDEX, sCurrentAccountIndex);
        editor.putInt(WEIBO_GROUP, sWeiboGroup);
        editor.putInt(PICTURE_QUALITY, sPictureQuality);
        editor.putInt(PICTURE_WIFI_QUALITY, sPictureWifiQuality);
        editor.putInt(ATME_FILTER, sAtmeFilter);
        editor.putInt(COMMENT_FILTER, sCommentFilter);
        editor.putBoolean(FAST_SCROLL_ENABLED, sFastScrollEnabled);
        editor.putInt(FONT_SIZE_MODE, sFontSizeMode);
        editor.putInt(AVATAR_QUALITY, sAvatarQuality);
        editor.putBoolean(NO_PICTURE_MODE, sNoPictureMode);
        editor.putInt(LOAD_WEIBO_COUNT_MODE, sLoadWeiboCountMode);
        editor.putInt(COMMENT_REPOST_LIST_AVATAR_MODE, sCommentRepostListAvatarMode);
        editor.putInt(NOTIFICATION_FREQUENCY, sNotificationFrequency);
        editor.putBoolean(NOTIFICATION_ENABLED, sNotificationEnabled);
        editor.putBoolean(NOTIFICATION_MENTION_WEIBO_ENABLED, sNotificationMentionWeiboEnabled);
        editor.putBoolean(NOTIFICATION_COMMENT_ENABLED, sNotificationCommentEnabled);
        editor.putBoolean(NOTIFICATION_MENTION_COMMENT_ENABLED, sNotificationMentionCommentEnabled);
        editor.putBoolean(NOTIFICATION_VIBRATE_ENABLED, sNotificationVibrateEnabled);
        editor.putBoolean(NOTIFICATION_LED_ENABLED, sNotificationLedEnabled);
        editor.putString(NOTIFICATION_RINGTONE, sNotificationRingtone);
        editor.putBoolean(WIFI_AUTO_DOWNLOAD_PIC_ENABLED, sWifiAutoDownloadPicEnabled);
        editor.putBoolean(LIST_HW_ACCEL_ENABLED, sListHwAccelEnabled);
        editor.putBoolean(PIC_HW_ACCEL_ENABLED, sPicHwAccelEnabled);
        editor.putInt(SCREEN_ORIENTATION, sScreenOrientation);
        editor.commit();
    }
    
    public static GlobalContext getInstance() {
        return sInstance;
    }
    
    public static AbsActivity getActivity() {
        return sActivity;
    }
    
    public static synchronized void setActivity(AbsActivity activity) {
        sActivity = activity;
    }
    
    public static int getAppTheme() {
        return sTheme;
    }
    
    public static synchronized void setAppTheme(int theme) {
        sTheme = theme;
    }
    
    public static int getCurrentAccountIndex() {
        return sCurrentAccountIndex;
    }
    
    public static synchronized void setCurrentAccountIndex(int currentAccountIndex) {
        sCurrentAccountIndex = currentAccountIndex;
    }
    
    public static int getWeiboGroup() {
        return sWeiboGroup;
    }
    
    public static synchronized void setWeiboGroup(int group) {
        sWeiboGroup = group;
    }
    
    public static int getPictureQuality() {
        return sPictureQuality;
    }
    
    public static synchronized void setPictureQuality(int pictureQuality) {
        sPictureQuality = pictureQuality;
    }
    
    public static int getPictureWifiQuality() {
        return sPictureWifiQuality;
    }
    
    public static synchronized void setPictureWifiQuality(int pictureWifiQuality) {
        sPictureWifiQuality = pictureWifiQuality;
    }
    
    public static int getAtmeFilter() {
        return sAtmeFilter;
    }
    
    public static synchronized void setAtmeFilter(int atmeFilter) {
        sAtmeFilter = atmeFilter;
    }
    
    public static int getCommentFilter() {
        return sCommentFilter;
    }
    
    public static synchronized void setCommentFilter(int commentFilter) {
        sCommentFilter = commentFilter;
    }
    
    public static boolean isFastScrollEnabled() {
        return sFastScrollEnabled;
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
    
    public static synchronized Account getAccount(int index) {
        return sAccounts.get(index);
    }
    
    public static synchronized Account getCurrentAccount() {
        if (sCurrentAccountIndex < sAccounts.size() && sCurrentAccountIndex > -1) {
            return sAccounts.get(sCurrentAccountIndex);
        } else {
            return null;
        }
    }
    
    public static synchronized int getAccountCount() {
        return sAccounts.size();
    }
    
    public static synchronized Account[] getAccounts() {
        return sAccounts.toArray(new Account[sAccounts.size()]);
    }
    
    public static synchronized void addOrUpdateAccount(Account account) {
        for (Account a : sAccounts) {
            if (a.user.id == account.user.id) {
                sAccounts.remove(a);
                break;
            }
        }
        sAccounts.add(account);
        sCurrentAccountIndex = sAccounts.indexOf(account);
        DatabaseUtils.insertOrUpdateAccount(account);
    }
    
    public static synchronized void removeAccount(int index) {
        Account account = sAccounts.remove(index);
        if (sCurrentAccountIndex >= sAccounts.size() && sAccounts.size() > 0) {
            sCurrentAccountIndex = sAccounts.size() - 1;
        }
        DatabaseUtils.removeAccount(account.user.id);
    }
    
    public static synchronized int indexOfAccount(Account account) {
        return sAccounts.indexOf(account);
    }
    
    public static void runOnUiThread(Runnable runnable) {
        sHandler.post(runnable);
    }
    
    public static void runOnUiThread(Runnable runnable, long delayMillis) {
        sHandler.postDelayed(runnable, delayMillis);
    }
    
    public static boolean isInWifi() {
        return ((ConnectivityManager) sInstance.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }
    
    public static String getSdCacheDir() {
        File file = sInstance.getExternalCacheDir();
        return file.getAbsolutePath();
    }
    
    public static LruCache<String, Bitmap> getBitmapCache() {
        return sBitmapCache;
    }
    
    private static void buildEmotionMap() {
        List<String> indexes = new ArrayList<String>();
        indexes.addAll(sWeiboEmotionNameMap.keySet());
        int emotionSize = (int) sInstance.getResources().getDimension(R.dimen.emotion_size);
        for (String index : indexes) {
            String fileName = sWeiboEmotionNameMap.get(index);
            AssetManager assetManager = sInstance.getAssets();
            InputStream is = null;
            try {
                is = assetManager.open(fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, emotionSize, emotionSize, true);
                    if (bitmap != scaledBitmap) {
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                    sEmotionMap.put(index, bitmap);
                }
            } catch (IOException e) {
                Logger.logExcpetion(e);
            } finally {
                Utilities.closeSilently(is);
            }
        }
    }
    
    public static Bitmap getEmotion(String index) {
        return sEmotionMap.get(index);
    }
    
    public static Map<String, String> getEmotionNameMap() {
        return sWeiboEmotionNameMap;
    }
    
    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sInstance);
    }
}
