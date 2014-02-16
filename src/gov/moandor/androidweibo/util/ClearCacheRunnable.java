package gov.moandor.androidweibo.util;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClearCacheRunnable implements Runnable {
    private static final long LAST_TIME = 1000 * 60 * 60 * 24 * 7;
    private static final String TAG = ClearCacheRunnable.class.getSimpleName();
    
    private List<String> mSkipPaths;
    
    @Override
    public void run() {
        Logger.debug(TAG, "started");
        mSkipPaths = getSkipPaths();
        File file = new File(FileUtils.WEIBO_PICTURE_CACHE);
        if (file.exists()) {
            clear(file);
        }
        file = new File(FileUtils.WEIBO_AVATAR_CACHE);
        if (file.exists()) {
            clear(file);
        }
        Logger.debug(TAG, "finished");
    }
    
    private void clear(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                clear(file);
            } else {
                if (System.currentTimeMillis() - file.lastModified() > LAST_TIME) {
                    String path = file.getAbsolutePath();
                    if (mSkipPaths == null || !mSkipPaths.contains(path)) {
                        file.delete();
                    }
                }
            }
        }
    }
    
    private static List<String> getSkipPaths() {
        if (GlobalContext.isInWifi()) {
            List<String> paths;
            try {
                paths = fetchSkipPaths();
                DatabaseUtils.updateFollowingAvatarPaths(paths);
                return paths;
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
            } catch (JSONException e) {
                Logger.logExcpetion(e);
            }
            return null;
        } else {
            return DatabaseUtils.getFollowingAvatarPaths();
        }
    }
    
    private static List<String> fetchSkipPaths() throws WeiboException, JSONException {
        Logger.debug(TAG, "started fetching following");
        String url = HttpUtils.UrlHelper.FRIENDSHIPS_FRIENDS;
        List<String> result = new ArrayList<String>();
        for (Account account : GlobalContext.getAccounts()) {
            HttpParams params = new HttpParams();
            params.addParam("access_token", account.token);
            params.addParam("uid", String.valueOf(account.user.id));
            params.addParam("count", "200");
            int nextCursor = 0;
            do {
                params.addParam("cursor", String.valueOf(nextCursor));
                String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
                JSONObject json = new JSONObject(response);
                List<WeiboUser> users = Utilities.getWeiboUsersFromJson(json);
                for (WeiboUser user : users) {
                    String path = FileUtils.getImagePathFromUrl(user.avatarLargeUrl, 
                            ImageDownloader.ImageType.AVATAR_LARGE);
                    if (!result.contains(path)) {
                        result.add(path);
                    }
                    path = FileUtils.getImagePathFromUrl(user.profileImageUrl, 
                            ImageDownloader.ImageType.AVATAR_SMALL);
                    if (!result.contains(path)) {
                        result.add(path);
                    }
                }
                nextCursor = json.getInt("next_cursor");
            } while (nextCursor != 0);
            Logger.debug(TAG, "finished fetching " + account.user.name + "'s following, " + result.size());
        }
        return result;
    }
}
