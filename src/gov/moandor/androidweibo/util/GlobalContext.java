package gov.moandor.androidweibo.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
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
    private static final int MIN_CACHE_SIZE = 1024 * 1024 * 8;
    
    private static volatile GlobalContext sInstance;
    private static volatile AbsActivity sActivity;
    private static Handler sHandler;
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
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        PreferenceManager.setDefaultValues(this, R.xml.prefs_notifications, false);
        PreferenceManager.setDefaultValues(this, R.xml.prefs_bm, false);
        CrashHandler.register();
        switch (ConfigManager.getAppTheme()) {
        case ConfigManager.THEME_LIGHT:
            setTheme(R.style.Theme_Weibo_Light);
            break;
        case ConfigManager.THEME_DARK:
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
        if (Utilities.isBmEnabled() && GlobalContext.isInWifi()) {
            new UpdateFollowingIdsTask().execute();
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
    
    public static GlobalContext getInstance() {
        return sInstance;
    }
    
    public static AbsActivity getActivity() {
        return sActivity;
    }
    
    public static synchronized void setActivity(AbsActivity activity) {
        sActivity = activity;
    }
    
    public static synchronized Account getAccount(int index) {
        return sAccounts.get(index);
    }
    
    public static synchronized Account getCurrentAccount() {
        int index = ConfigManager.getCurrentAccountIndex();
        if (index < sAccounts.size() && index > -1) {
            return sAccounts.get(index);
        } else {
            return null;
        }
    }
    
    public static synchronized int getAccountCount() {
        return sAccounts.size();
    }
    
    public static synchronized Account[] getAccounts() {
        return sAccounts.toArray(new Account[0]);
    }
    
    public static synchronized void addOrUpdateAccount(final Account account) {
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseUtils.insertOrUpdateAccount(account);
            }
        });
        for (int i = 0; i < sAccounts.size(); i++) {
            if (sAccounts.get(i).user.id == account.user.id) {
                sAccounts.set(i, account);
                return;
            }
        }
        sAccounts.add(account);
        ConfigManager.setCurrentAccountIndex(sAccounts.indexOf(account));
    }
    
    public static synchronized void removeAccount(int index) {
        Account account = sAccounts.remove(index);
        if (ConfigManager.getCurrentAccountIndex() >= sAccounts.size() && sAccounts.size() > 0) {
            ConfigManager.setCurrentAccountIndex(sAccounts.size() - 1);
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
}
