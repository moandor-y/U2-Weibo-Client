package gov.moandor.androidweibo.util;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.dao.FollowingDao;

public class ClearCacheRunnable implements Runnable {
    private static final long DURATION = 1000 * 60 * 60 * 24 * 3;

    private List<String> mSkipPaths;

    private static List<String> getSkipPaths() {
        if (GlobalContext.isInWifi()) {
            List<String> paths;
            try {
                paths = fetchSkipPaths();
                DatabaseUtils.updateFollowingAvatarPaths(paths);
                return paths;
            } catch (WeiboException e) {
                Logger.logException(e);
            } catch (JSONException e) {
                Logger.logException(e);
            }
            return null;
        } else {
            return DatabaseUtils.getFollowingAvatarPaths();
        }
    }

    private static List<String> fetchSkipPaths() throws WeiboException, JSONException {
        List<String> result = new ArrayList<String>();
        for (Account account : GlobalContext.getAccounts()) {
            FollowingDao dao = new FollowingDao();
            dao.setToken(account.token);
            dao.setUid(account.user.id);
            dao.setCount(200);
            int nextCursor = 0;
            do {
                dao.setCursor(nextCursor);
                List<WeiboUser> users = dao.execute();
                users.add(account.user);
                for (WeiboUser user : users) {
                    String path =
                            FileUtils.getImagePathFromUrl(user.avatarLargeUrl, ImageDownloader.ImageType.AVATAR_LARGE);
                    if (!result.contains(path)) {
                        result.add(path);
                    }
                    path = FileUtils.getImagePathFromUrl(user.profileImageUrl, ImageDownloader.ImageType.AVATAR_SMALL);
                    if (!result.contains(path)) {
                        result.add(path);
                    }
                }
                nextCursor = dao.getNextCursor();
            } while (nextCursor != 0);
        }
        return result;
    }

    @Override
    public void run() {
        mSkipPaths = getSkipPaths();
        File file = new File(ConfigManager.getPictureCacheDir());
        if (file.exists()) {
            clear(file);
        }
        file = new File(ConfigManager.getAvatarCacheDir());
        if (file.exists()) {
            clear(file);
        }
    }

    private void clear(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                clear(file);
            } else {
                if (System.currentTimeMillis() - file.lastModified() > DURATION) {
                    String path = file.getAbsolutePath();
                    if (mSkipPaths == null || !mSkipPaths.contains(path)) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
    }
}
