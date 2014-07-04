package gov.moandor.androidweibo.util;

import android.content.Intent;

import gov.moandor.androidweibo.activity.AtUserActivity;
import gov.moandor.androidweibo.activity.AuthorizeActivity;
import gov.moandor.androidweibo.activity.DmActivity;
import gov.moandor.androidweibo.activity.DraftBoxActivity;
import gov.moandor.androidweibo.activity.FavoritesActivity;
import gov.moandor.androidweibo.activity.HackLoginActivity;
import gov.moandor.androidweibo.activity.ImageViewerActivity;
import gov.moandor.androidweibo.activity.UserActivity;
import gov.moandor.androidweibo.activity.UserListActivity;
import gov.moandor.androidweibo.activity.UserWeiboListActivity;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.activity.WriteCommentActivity;
import gov.moandor.androidweibo.activity.WriteWeiboActivity;
import gov.moandor.androidweibo.bean.AbsDraftBean;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;

public class ActivityUtils {
    public static Intent weiboActivity(WeiboStatus status) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), getWeiboActivityClass());
        intent.putExtra(WeiboActivity.WEIBO_STATUS, status);
        return intent;
    }

    private static Class<?> getWeiboActivityClass() {
        if (ConfigManager.isSwipeBackEnabled()) {
            return WeiboActivity.Translucent.class;
        } else {
            return WeiboActivity.class;
        }
    }

    public static Intent userActivity(String userName) {
        return userActivity(null, userName, 0);
    }

    public static Intent userActivity(WeiboUser user) {
        return userActivity(user, null, 0);
    }

    public static Intent userActivity(long userId) {
        return userActivity(null, null, userId);
    }

    private static Intent userActivity(WeiboUser user, String userName, long userId) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), UserActivity.class);
        intent.putExtra(UserActivity.USER, user);
        intent.putExtra(UserActivity.USER_NAME, userName);
        intent.putExtra(UserActivity.USER_ID, userId);
        return intent;
    }

    public static Intent userActivityFromDomain(String domain) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), UserActivity.class);
        intent.putExtra(UserActivity.USER_DOMAIN, domain);
        return intent;
    }

    public static Intent atUserActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), AtUserActivity.class);
        return intent;
    }

    public static Intent authorizeActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), AuthorizeActivity.class);
        return intent;
    }

    public static Intent dmConversationActivity(WeiboUser user) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), DmActivity.ConversationActivity.class);
        intent.putExtra(DmActivity.ConversationActivity.USER, user);
        return intent;
    }

    public static Intent dmActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), DmActivity.class);
        return intent;
    }

    public static Intent draftBoxActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), DraftBoxActivity.class);
        return intent;
    }

    public static Intent favoritesActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), FavoritesActivity.class);
        return intent;
    }

    public static Intent hackLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), HackLoginActivity.class);
        return intent;
    }

    public static Intent imageViewerActivity(WeiboStatus status, int position) {
        ImageDownloader.ImageType type = Utilities.getDetailPictureType();
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.IMAGE_TYPE, type);
        intent.putExtra(ImageViewerActivity.POSITION, position);
        intent.putExtra(ImageViewerActivity.WEIBO_STATUS, status);
        return intent;
    }

    public static Intent settingsActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), CompatUtils.getSettingsActivity());
        return intent;
    }

    public static Intent userListActivity(WeiboUser user, UserListActivity.Type type) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), UserListActivity.class);
        intent.putExtra(UserListActivity.USER, user);
        intent.putExtra(UserListActivity.TYPE, type);
        return intent;
    }

    public static Intent userWeiboListActivity(WeiboUser user) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), UserWeiboListActivity.class);
        intent.putExtra(UserWeiboListActivity.USER, user);
        return intent;
    }

    public static Intent writeCommentActivity(AbsDraftBean draft) {
        return writeCommentActivity(draft, null, null);
    }

    public static Intent writeCommentActivity(WeiboStatus commentedStatus) {
        return writeCommentActivity(null, commentedStatus, null);
    }

    public static Intent writeCommentActivity(WeiboStatus commentedStatus, WeiboComment repliedComment) {
        return writeCommentActivity(null, commentedStatus, repliedComment);
    }

    private static Intent writeCommentActivity(AbsDraftBean draft, WeiboStatus commentedStatus,
                                               WeiboComment repliedComment) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteCommentActivity.class);
        intent.putExtra(WriteCommentActivity.DRAFT, draft);
        intent.putExtra(WriteCommentActivity.COMMENTED_WEIBO_STATUS, commentedStatus);
        intent.putExtra(WriteCommentActivity.REPLIED_WEIBO_COMMENT, repliedComment);
        return intent;
    }

    public static Intent writeWeiboActivity(AbsDraftBean draft) {
        return writeWeiboActivity(draft, null);
    }

    public static Intent writeWeiboActivity() {
        return writeWeiboActivity(null, null);
    }

    public static Intent writeWeiboActivity(WeiboStatus retweetedStatus) {
        return writeWeiboActivity(null, retweetedStatus);
    }

    private static Intent writeWeiboActivity(AbsDraftBean draft, WeiboStatus retweetedStatus) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteWeiboActivity.class);
        intent.putExtra(WriteWeiboActivity.DRAFT, draft);
        intent.putExtra(WriteWeiboActivity.RETWEET_WEIBO_STATUS, retweetedStatus);
        return intent;
    }
}
