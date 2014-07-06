package gov.moandor.androidweibo.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Browser;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.AccountIdDao;
import gov.moandor.androidweibo.dao.UserShowDao;
import gov.moandor.androidweibo.util.filter.UserWeiboFilter;

public class Utilities {
    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public static Map<String, String> parseUrl(String urlAddress) {
        try {
            URL url = new URL(urlAddress);
            Map<String, String> map = decodeUrl(url.getQuery());
            Map<String, String> ref = decodeUrl(url.getRef());
            for (String key : ref.keySet()) {
                map.put(key, ref.get(key));
            }
            return map;
        } catch (MalformedURLException e) {
            Logger.logException(e);
            return null;
        }
    }

    private static Map<String, String> decodeUrl(String s) {
        Map<String, String> params = new HashMap<String, String>();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                try {
                    params.put(URLDecoder.decode(v[0], "UTF-8"), URLDecoder.decode(v[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Logger.logException(e);
                }
            }
        }
        return params;
    }

    public static String encodeUrl(Map<String, String> param) {
        if (param == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (String key : param.keySet()) {
            String value = param.get(key);
            if (!TextUtils.isEmpty(value)) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append("&");
                }
                stringBuilder.append(encodeUrl(key)).append("=").append(encodeUrl(value));
            }
        }
        return stringBuilder.toString();
    }

    public static String encodeUrl(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.logException(e);
        }
        return null;
    }

    public static float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, GlobalContext.getInstance().getResources()
                .getDisplayMetrics());
    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, GlobalContext.getInstance()
                .getResources().getDisplayMetrics());
    }

    public static void openUri(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        if (Utilities.isIntentAvailable(intent)) {
            context.startActivity(intent);
        }
    }

    public static int getColor(int attrId) {
        int[] attr = new int[]{attrId};
        Context context = GlobalContext.getActivity();
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    public static int getResourceId(int attrId) {
        int[] attr = new int[]{attrId};
        Context context = GlobalContext.getActivity();
        TypedArray typedArray = context.obtainStyledAttributes(attr);
        int resId = typedArray.getResourceId(0, 0);
        typedArray.recycle();
        return resId;
    }

    public static ImageDownloader.ImageType getAvatarType() {
        switch (ConfigManager.getAvatarQuality()) {
            case ConfigManager.AVATAR_AUTO:
                if (GlobalContext.isInWifi()) {
                    return ImageDownloader.ImageType.AVATAR_LARGE;
                } else {
                    return ImageDownloader.ImageType.AVATAR_SMALL;
                }
            case ConfigManager.AVATAR_LARGE:
                return ImageDownloader.ImageType.AVATAR_LARGE;
            case ConfigManager.AVATAR_SMALL:
                return ImageDownloader.ImageType.AVATAR_SMALL;
            default:
                return null;
        }
    }

    public static ImageDownloader.ImageType getListPictureType() {
        int imageQuality;
        if (GlobalContext.isInWifi()) {
            imageQuality = ConfigManager.getPictureWifiQuality();
        } else {
            imageQuality = ConfigManager.getPictureQuality();
        }
        switch (imageQuality) {
            case ConfigManager.PICTURE_LARGE:
                return ImageDownloader.ImageType.PICTURE_LARGE;
            case ConfigManager.PICTURE_MEDIUM:
                return ImageDownloader.ImageType.PICTURE_MEDIUM;
            case ConfigManager.PICTURE_SMALL:
                return ImageDownloader.ImageType.PICTURE_SMALL;
            default:
                return null;
        }
    }

    public static ImageDownloader.ImageType getDetailPictureType() {
        switch (getListPictureType()) {
            case PICTURE_MEDIUM:
            case PICTURE_SMALL:
                return ImageDownloader.ImageType.PICTURE_MEDIUM;
            case PICTURE_LARGE:
                return ImageDownloader.ImageType.PICTURE_LARGE;
            default:
                return null;
        }
    }

    public static int getLoadWeiboCount() {
        int loadWeiboCountMode = ConfigManager.getLoadWeiboCountMode();
        if (loadWeiboCountMode > 0) {
            return loadWeiboCountMode * 25;
        } else {
            if (GlobalContext.isInWifi()) {
                return ConfigManager.LOAD_WEIBO_COUNT_MORE;
            } else {
                return ConfigManager.LOAD_WEIBO_COUNT_FEWER;
            }
        }
    }

    public static int getScreenWidth() {
        return GlobalContext.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return GlobalContext.getInstance().getResources().getDisplayMetrics().heightPixels;
    }

    public static void notice(final CharSequence message) {
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GlobalContext.getInstance(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void notice(final int resId) {
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GlobalContext.getInstance(), resId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void notice(final int resId, final Object... formatArgs) {
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = GlobalContext.getInstance().getString(resId, formatArgs);
                Toast.makeText(GlobalContext.getInstance(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static <T> SparseArray<T> toSparseArray(List<T> in) {
        SparseArray<T> result = new SparseArray<T>();
        int size = in.size();
        for (int i = 0; i < size; i++) {
            result.append(i, in.get(i));
        }
        return result;
    }

    public static float getFontSize() {
        switch (ConfigManager.getFontSizeMode()) {
            case ConfigManager.FONT_SIZE_MODE_SMALL:
                return ConfigManager.FONT_SIZE_SMALL;
            default:
            case ConfigManager.FONT_SIZE_MODE_MEDIUM:
                return ConfigManager.FONT_SIZE_MEDIUM;
            case ConfigManager.FONT_SIZE_MODE_LARGE:
                return ConfigManager.FONT_SIZE_LARGE;
        }
    }

    public static boolean isCommentRepostListAvatarEnabled() {
        switch (ConfigManager.getCommentRepostListAvatarMode()) {
            default:
            case ConfigManager.COMMENT_REPOST_LIST_AVATAR_AUTO:
                return GlobalContext.isInWifi();
            case ConfigManager.COMMENT_REPOST_LIST_AVATAR_ENABLED:
                return true;
            case ConfigManager.COMMENT_REPOST_LIST_AVATAR_DISABLED:
                return false;
        }
    }

    public static void hideKeyboard(IBinder windowToken) {
        InputMethodManager imm =
                (InputMethodManager) GlobalContext.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public static void showKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) GlobalContext.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static int sendLength(String string) {
        int len = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.substring(i, i + 1).matches("[Α-￥]")) {
                len += 2;
            } else {
                len++;
            }
        }
        if (len % 2 == 1) {
            len = len / 2 + 1;
        } else {
            len = len / 2;
        }
        return len;
    }

    public static boolean isIntentAvailable(Intent intent) {
        PackageManager packageManager = GlobalContext.getInstance().getPackageManager();
        List<ResolveInfo> resolveInfos =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfos.size() > 0;
    }

    public static PendingIntent newEmptyPendingIntent() {
        return PendingIntent.getActivity(GlobalContext.getInstance(), 0, new Intent(),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void copyText(String text) {
        ClipboardManager clipboardManager =
                (ClipboardManager) GlobalContext.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("sinaweibo", text));
        Utilities.notice(R.string.copied_successfully);
    }

    public static boolean isScreenLandscape() {
        int orientation = GlobalContext.getInstance().getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return GlobalContext.getInstance().registerReceiver(receiver, filter);
    }

    public static void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            GlobalContext.getInstance().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Logger.logException(e);
        }
    }

    public static void fetchAndSaveAccountInfo(String token) throws WeiboException {
        AccountIdDao getAccountIdDao = new AccountIdDao();
        getAccountIdDao.setToken(token);
        long id = getAccountIdDao.execute();
        UserShowDao userShowDao = new UserShowDao();
        userShowDao.setToken(token);
        userShowDao.setUid(id);
        Account account = new Account();
        account.token = token;
        account.user = userShowDao.execute();
        GlobalContext.addOrUpdateAccount(account);
    }

    public static void registerShareActionMenu(MenuItem item, AbsItemBean bean) {
        WeiboUser user = bean.weiboUser;
        if (user == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "@" + user.name + " : " + bean.text);
        if (isIntentAvailable(intent)) {
            ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            provider.setShareIntent(intent);
        }
    }

    public static long getNotificationInterval() {
        int mode;
        if (GlobalContext.isInWifi()) {
            mode = ConfigManager.getNotificationWifiFrequency();
        } else {
            mode = ConfigManager.getNotificationFrequency();
        }
        switch (mode) {
            case ConfigManager.THREE_MINUTES:
                return 3 * 60 * 1000;
            case ConfigManager.FIFTEEN_MINUTES:
            default:
                return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            case ConfigManager.HALF_HOUR:
                return AlarmManager.INTERVAL_HALF_HOUR;
        }
    }

    public static String buildIntentExtraName(String name) {
        String packageName = GlobalContext.getInstance().getPackageName();
        return packageName + "." + name;
    }

    public static boolean isWeiboMidUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        url = convertWeiboCnToCom(url);
        if (!(url.startsWith(UrlHelper.WEIBO_COM) || url.startsWith(UrlHelper.E_WEIBO_COM))) {
            return false;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.contains(UrlHelper.WEIBO_COM + "/")) {
            url = url.substring(UrlHelper.WEIBO_COM.length() + 1, url.length());
        } else if (url.contains(UrlHelper.E_WEIBO_COM + "/")) {
            url = url.substring(UrlHelper.E_WEIBO_COM.length() + 1, url.length());
        }
        String[] result = url.split("/");
        return result.length == 2;
    }

    public static String getMidFromUrl(String url) {
        url = convertWeiboCnToCom(url);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        int end = url.length();
        if (url.contains("?")) {
            end = url.indexOf("?");
        }
        if (url.contains(UrlHelper.WEIBO_COM + "/")) {
            url = url.substring(UrlHelper.WEIBO_COM.length() + 1, end);
        } else {
            url = url.substring(UrlHelper.E_WEIBO_COM.length() + 1, end);
        }
        return url.split("/")[1];
    }

    public static boolean isWeiboAccountDomainLink(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        url = convertWeiboCnToCom(url);
        if (!(url.startsWith(UrlHelper.WEIBO_COM + "/"))
                && !(url.startsWith(UrlHelper.E_WEIBO_COM + "/"))) {
            return false;
        }
        if (url.contains("?")) {
            return false;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.lastIndexOf("/"));
        }
        int count = 0;
        char[] value = url.toCharArray();
        for (char c : value) {
            if ("/".equalsIgnoreCase(String.valueOf(c))) {
                count++;
            }
        }
        return count == 3 && !url.equalsIgnoreCase("http://weibo.com/pub");
    }

    public static String getDomainFromWeiboAccountLink(String url) {
        url = convertWeiboCnToCom(url);
        String domain = null;
        if (url.startsWith(UrlHelper.WEIBO_COM)) {
            domain = url.substring(UrlHelper.WEIBO_COM.length() + 1);
        } else if (url.startsWith(UrlHelper.E_WEIBO_COM)) {
            domain = url.substring(UrlHelper.E_WEIBO_COM.length() + 1);
        }
        return domain == null ? null : domain.replace("/", "");
    }

    public static boolean isWeiboAccountIdLink(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith(UrlHelper.WEIBO_USER_ID_PREFIX);
    }

    public static long getIdFromWeiboAccountLink(String url) {
        url = convertWeiboCnToCom(url);
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        String idStr = url.substring(UrlHelper.WEIBO_USER_ID_PREFIX.length());
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            Logger.logException(e);
            return 0;
        }
    }

    public static String getVersionName() {
        GlobalContext context = GlobalContext.getInstance();
        String packageName = context.getPackageName();
        PackageManager manager = context.getPackageManager();
        try {
            return manager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void ignoreUser(final WeiboUser user) {
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                UserWeiboFilter filter = new UserWeiboFilter();
                filter.setUser(user);
                DatabaseUtils.insertOrUpdateWeiboFilter(filter);
            }
        });
    }

    private static String convertWeiboCnToCom(String url) {
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith(UrlHelper.WEIBO_CN)) {
                return url.replace(UrlHelper.WEIBO_CN, UrlHelper.WEIBO_COM);
            } else if (url.startsWith(UrlHelper.WWW_WEIBO_COM)) {
                return url.replace(UrlHelper.WWW_WEIBO_COM, UrlHelper.WEIBO_COM);
            } else if (url.startsWith(UrlHelper.WWW_WEIBO_CN)) {
                return url.replace(UrlHelper.WWW_WEIBO_CN, UrlHelper.WEIBO_COM);
            }
        }
        return url;
    }
}
