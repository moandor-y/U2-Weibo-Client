package gov.moandor.androidweibo.util;

import android.annotation.TargetApi;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboGeo;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilities {
    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            Logger.logExcpetion(e);
        }
    }
    
    public static List<WeiboStatus> getWeiboStatusesFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray statuses = jsonStatuses.getJSONArray("statuses");
            int len = statuses.length();
            List<WeiboStatus> weiboStatuses = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                weiboStatuses.add(getWeiboStatusFromJson(statuses.getJSONObject(i)));
            }
            return weiboStatuses;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    public static List<WeiboStatus> getWeiboRepostsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray statuses = jsonStatuses.getJSONArray("reposts");
            int len = statuses.length();
            List<WeiboStatus> weiboStatuses = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                weiboStatuses.add(getWeiboStatusFromJson(statuses.getJSONObject(i)));
            }
            return weiboStatuses;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    public static List<WeiboStatus> getFavoritesFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray favorites = json.getJSONArray("favorites");
            int len = favorites.length();
            List<WeiboStatus> result = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                result.add(getWeiboStatusFromJson(favorites.getJSONObject(i).getJSONObject("status")));
            }
            return result;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    public static WeiboStatus getWeiboStatusFromJson(JSONObject json) {
        WeiboStatus weiboStatus = new WeiboStatus();
        weiboStatus.createdAt = json.optString("created_at", null);
        weiboStatus.id = json.optLong("id");
        weiboStatus.mid = json.optLong("mid");
        weiboStatus.text = json.optString("text", null);
        weiboStatus.source = json.optString("source", null);
        weiboStatus.favorited = json.optBoolean("favorited");
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            weiboStatus.weiboUser = getWeiboUserFromJson(user);
        }
        weiboStatus.repostCount = json.optInt("reposts_count");
        weiboStatus.commentCount = json.optInt("comments_count");
        weiboStatus.attitudeCount = json.optInt("attitudes_count");
        JSONObject retweetedStatusObj = json.optJSONObject("retweeted_status");
        if (retweetedStatusObj != null) {
            weiboStatus.retweetStatus = getWeiboStatusFromJson(retweetedStatusObj);
        }
        JSONArray urls = json.optJSONArray("pic_urls");
        if (urls != null) {
            int count = urls.length();
            weiboStatus.picCount = count;
            weiboStatus.thumbnailPic = new String[count];
            weiboStatus.bmiddlePic = new String[count];
            weiboStatus.originalPic = new String[count];
            for (int i = 0; i < count; i++) {
                JSONObject url = urls.optJSONObject(i);
                weiboStatus.thumbnailPic[i] = url.optString("thumbnail_pic", null);
                weiboStatus.bmiddlePic[i] = weiboStatus.thumbnailPic[i].replace("thumbnail", "bmiddle");
                weiboStatus.originalPic[i] = weiboStatus.thumbnailPic[i].replace("thumbnail", "large");
            }
        }
        JSONObject geo = json.optJSONObject("geo");
        if (geo != null) {
            weiboStatus.weiboGeo = getWeiboGeoFromJson(geo);
        }
        return weiboStatus;
    }
    
    public static List<WeiboUser> getWeiboUsersFromJson(JSONObject json) throws WeiboException {
        try {
            JSONArray users = json.getJSONArray("users");
            int len = users.length();
            List<WeiboUser> weiboUsers = new ArrayList<WeiboUser>();
            for (int i = 0; i < len; i++) {
                weiboUsers.add(getWeiboUserFromJson(users.getJSONObject(i)));
            }
            return weiboUsers;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    public static WeiboUser getWeiboUserFromJson(String jsonStr) throws WeiboException {
        try {
            return getWeiboUserFromJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    private static WeiboUser getWeiboUserFromJson(JSONObject json) {
        WeiboUser result = new WeiboUser();
        result.id = json.optLong("id");
        result.name = json.optString("name", null);
        result.location = json.optString("location", null);
        result.description = json.optString("description", null);
        result.profileImageUrl = json.optString("profile_image_url", null);
        result.gender = json.optString("gender", null);
        result.followersCount = json.optInt("followers_count");
        result.friendsCount = json.optInt("friends_count");
        result.statusesCount = json.optInt("statuses_count");
        result.following = json.optBoolean("following");
        result.allowAllActMsg = json.optBoolean("allow_all_act_msg");
        result.verified = json.optBoolean("verified");
        result.remark = json.optString("remark", null);
        result.allowAllComment = json.optBoolean("allow_all_comment");
        result.avatarLargeUrl = json.optString("avatar_large", null);
        result.verifiedReason = json.optString("verified_reason", null);
        result.followMe = json.optBoolean("follow_me");
        result.onlineStatus = json.optInt("online_status");
        return result;
    }
    
    private static WeiboGeo getWeiboGeoFromJson(JSONObject json) {
        WeiboGeo weiboGeo = new WeiboGeo();
        weiboGeo.cityName = json.optString("city_name", null);
        weiboGeo.provinceName = json.optString("province_name", null);
        weiboGeo.address = json.optString("address", null);
        return weiboGeo;
    }
    
    public static long getWeiboAccountIdFromJson(String jsonStr) throws WeiboException {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
            return json.getLong("uid");
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
        
    }
    
    public static List<DirectMessagesUser> getDmUsersFromJson(JSONObject json) throws WeiboException {
        List<DirectMessagesUser> result = new ArrayList<DirectMessagesUser>();
        try {
            JSONArray userList = json.getJSONArray("user_list");
            int len = userList.length();
            for (int i = 0; i < len; i++) {
                JSONObject user = userList.getJSONObject(i);
                result.add(getDmUserFromJson(user));
            }
            return result;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    private static DirectMessagesUser getDmUserFromJson(JSONObject json) throws JSONException {
        WeiboUser user = getWeiboUserFromJson(json.getJSONObject("user"));
        DirectMessage dm = getDirectMessageFromJson(json.getJSONObject("direct_message"));
        DirectMessagesUser result = new DirectMessagesUser();
        result.message = dm;
        result.user = user;
        result.unreadCount = json.getInt("unread_count");
        return result;
    }
    
    private static DirectMessage getDirectMessageFromJson(JSONObject json) throws JSONException {
        DirectMessage result = new DirectMessage();
        result.id = json.getLong("id");
        result.createdAt = json.getString("created_at");
        result.text = json.getString("text");
        result.sender = getWeiboUserFromJson(json.getJSONObject("sender"));
        result.recipient = getWeiboUserFromJson(json.getJSONObject("recipient"));
        return result;
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
            Logger.logExcpetion(e);
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
                    Logger.logExcpetion(e);
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
            Logger.logExcpetion(e);
        }
        return null;
    }
    
    public static List<WeiboComment> getWeiboCommentsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray comments = jsonStatuses.getJSONArray("comments");
            int len = comments.length();
            List<WeiboComment> weiboComments = new ArrayList<WeiboComment>();
            for (int i = 0; i < len; i++) {
                weiboComments.add(getWeiboCommentFromJson(comments.getJSONObject(i)));
            }
            return weiboComments;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    private static WeiboComment getWeiboCommentFromJson(JSONObject json) {
        WeiboComment weiboComment = new WeiboComment();
        weiboComment.createdAt = json.optString("created_at", null);
        weiboComment.id = json.optLong("id");
        weiboComment.text = json.optString("text", null);
        weiboComment.source = json.optString("source", null);
        weiboComment.mid = json.optLong("mid");
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            weiboComment.weiboUser = getWeiboUserFromJson(user);
        }
        JSONObject status = json.optJSONObject("status");
        if (status != null) {
            weiboComment.weiboStatus = getWeiboStatusFromJson(status);
        }
        JSONObject replied = json.optJSONObject("reply_comment");
        if (replied != null) {
            weiboComment.repliedComment = getWeiboCommentFromJson(replied);
        }
        return weiboComment;
    }
    
    public static UnreadCount getUnreadCountFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            UnreadCount result = new UnreadCount();
            result.weiboStatus = json.getInt("status");
            result.comment = json.getInt("cmt");
            result.mentionWeibo = json.getInt("mention_status");
            result.mentionComment = json.getInt("mention_cmt");
            return result;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    public static List<UserSuggestion> getUserSuggestionsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONArray json = new JSONArray(jsonStr);
            int len = json.length();
            List<UserSuggestion> result = new ArrayList<UserSuggestion>();
            for (int i = 0; i < len; i++) {
                UserSuggestion suggestion = new UserSuggestion();
                JSONObject jsonSuggestion = json.getJSONObject(i);
                suggestion.id = jsonSuggestion.getLong("uid");
                suggestion.nickname = jsonSuggestion.getString("nickname");
                suggestion.remark = jsonSuggestion.getString("remark");
                result.add(suggestion);
            }
            return result;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
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
        switch (GlobalContext.getAvatarQuality()) {
        case GlobalContext.AVATAR_AUTO:
            if (GlobalContext.isInWifi()) {
                return ImageDownloader.ImageType.AVATAR_LARGE;
            } else {
                return ImageDownloader.ImageType.AVATAR_SMALL;
            }
        case GlobalContext.AVATAR_LARGE:
            return ImageDownloader.ImageType.AVATAR_LARGE;
        case GlobalContext.AVATAR_SMALL:
            return ImageDownloader.ImageType.AVATAR_SMALL;
        default:
            return null;
        }
    }
    
    public static ImageDownloader.ImageType getListPictureType() {
        int imageQuality;
        if (GlobalContext.isInWifi()) {
            imageQuality = GlobalContext.getPictureWifiQuality();
        } else {
            imageQuality = GlobalContext.getPictureQuality();
        }
        switch (imageQuality) {
        case GlobalContext.PICTURE_LARGE:
            return ImageDownloader.ImageType.PICTURE_LARGE;
        case GlobalContext.PICTURE_MEDIUM:
            return ImageDownloader.ImageType.PICTURE_MEDIUM;
        case GlobalContext.PICTURE_SMALL:
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
        int loadWeiboCountMode = GlobalContext.getLoadWeiboCountMode();
        if (loadWeiboCountMode > 0) {
            return loadWeiboCountMode * 25;
        } else {
            if (GlobalContext.isInWifi()) {
                return GlobalContext.LOAD_WEIBO_COUNT_MORE;
            } else {
                return GlobalContext.LOAD_WEIBO_COUNT_FEWER;
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
        switch (GlobalContext.getFontSizeMode()) {
        case GlobalContext.FONT_SIZE_MODE_SMALL:
            return GlobalContext.FONT_SIZE_SMALL;
        default:
        case GlobalContext.FONT_SIZE_MODE_MEDIUM:
            return GlobalContext.FONT_SIZE_MEDIUM;
        case GlobalContext.FONT_SIZE_MODE_LARGE:
            return GlobalContext.FONT_SIZE_LARGE;
        }
    }
    
    public static boolean isCommentRepostListAvatarEnabled() {
        switch (GlobalContext.getCommentRepostListAvatarMode()) {
        default:
        case GlobalContext.COMMENT_REPOST_LIST_AVATAR_AUTO:
            return GlobalContext.isInWifi();
        case GlobalContext.COMMENT_REPOST_LIST_AVATAR_ENABLED:
            return true;
        case GlobalContext.COMMENT_REPOST_LIST_AVATAR_DISABLED:
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
            Logger.logExcpetion(e);
        }
    }
    
    public static void fetchAndSaveAccountInfo(String token) throws WeiboException {
        String url = HttpUtils.UrlHelper.ACCOUNT_GET_UID;
        HttpParams params = new HttpParams();
        params.addParam("access_token", token);
        String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
        long id = Utilities.getWeiboAccountIdFromJson(response);
        params.clear();
        url = HttpUtils.UrlHelper.USERS_SHOW;
        params.addParam("access_token", token);
        params.addParam("uid", String.valueOf(id));
        response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
        Account account = new Account();
        account.token = token;
        account.user = Utilities.getWeiboUserFromJson(response);
        GlobalContext.addOrUpdateAccount(account);
    }
    
    public static boolean isHackEnabled() {
        return GlobalContext.getInstance().getResources().getBoolean(R.bool.hack_enabled);
    }
    
    public static void registerShareActionMenu(MenuItem item, AbsItemBean bean) {
        if (bean == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        WeiboUser user = bean.weiboUser;
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "@" + user.name + " : " + bean.text);
        if (Utilities.isIntentAvailable(intent)) {
            ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            provider.setShareIntent(intent);
        }
    }
}
