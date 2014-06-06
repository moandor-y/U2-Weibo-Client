package gov.moandor.androidweibo.notification;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.dao.CommentsMentionsDao;
import gov.moandor.androidweibo.dao.CommentsToMeDao;
import gov.moandor.androidweibo.dao.DmUserListDao;
import gov.moandor.androidweibo.dao.MentionsWeiboTimelineDao;
import gov.moandor.androidweibo.dao.UnreadCountDao;
import gov.moandor.androidweibo.fragment.CommentListFragment;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class FetchUnreadMessageService extends IntentService {
    public FetchUnreadMessageService() {
        super(FetchUnreadMessageService.class.getSimpleName());
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ConfigManager.isNotificationEnabledAfterExit() && !MainActivity.isRunning()) {
            return;
        }
        List<Account> accounts = DatabaseUtils.getAccounts();
        for (Account account : accounts) {
            fetch(this, account);
        }
    }
    
    private static void fetch(Context context, Account account) {
        UnreadCountDao dao = new UnreadCountDao();
        dao.setToken(account.token);
        try {
            UnreadCount unreadCount = dao.execute();
            if (account.user.id == GlobalContext.getCurrentAccount().user.id) {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_UNREAD_UPDATED);
                intent.putExtra(MainActivity.UNREAD_COUNT, unreadCount);
                context.sendBroadcast(intent);
            }
            showNotification(context, account, unreadCount);
        } catch (WeiboException e) {
            Logger.logExcpetion(e);
        }
    }
    
    private static List<WeiboComment> fetchComments(Account account) throws WeiboException {
        List<WeiboComment> oldComments = DatabaseUtils.getComments(account.user.id, CommentListFragment.ALL);
        WeiboComment oldComment = null;
        if (oldComments.size() > 0) {
            oldComment = oldComments.get(0);
        }
        CommentsToMeDao dao = new CommentsToMeDao();
        dao.setToken(account.token);
        dao.setCount(1);
        if (oldComment != null) {
            dao.setSinceId(oldComment.id);
        }
        return dao.execute();
    }
    
    private static List<WeiboStatus> fetchMentionStatuses(Account account) throws WeiboException {
        List<WeiboStatus> oldStatuses = DatabaseUtils.getAtmeStatuses(account.user.id, 0);
        WeiboStatus oldStatus = null;
        if (oldStatuses.size() > 0) {
            oldStatus = oldStatuses.get(0);
        }
        MentionsWeiboTimelineDao dao = new MentionsWeiboTimelineDao();
        dao.setToken(account.token);
        dao.setCount(1);
        if (oldStatus != null) {
            dao.setSinceId(oldStatus.id);
        }
        return dao.execute();
    }
    
    private static List<WeiboComment> fetchMentionComments(Account account) throws WeiboException {
        List<WeiboComment> oldComments = DatabaseUtils.getComments(account.user.id, CommentListFragment.ATME);
        WeiboComment oldComment = null;
        if (oldComments.size() > 0) {
            oldComment = oldComments.get(0);
        }
        CommentsMentionsDao dao = new CommentsMentionsDao();
        dao.setToken(account.token);
        dao.setCount(1);
        if (oldComment != null) {
            dao.setSinceId(oldComment.id);
        }
        return dao.execute();
    }
    
    private static DirectMessage fetchDm(Account account) throws WeiboException {
        DmUserListDao dao = new DmUserListDao();
        dao.setToken(account.token);
        dao.setCount(1);
        List<DirectMessagesUser> users = dao.execute();
        if (users.size() >= 1) {
            DirectMessagesUser user = users.get(0);
            return user.message;
        }
        return null;
    }
    
    private static void showNotification(Context context, Account account, UnreadCount unreadCount)
            throws WeiboException {
        WeiboComment comment = null;
        if (unreadCount.comment > 0 && ConfigManager.isNotificationCommentEnabled()) {
            List<WeiboComment> comments = fetchComments(account);
            if (comments.size() > 0) {
                comment = comments.get(0);
            }
        }
        WeiboStatus mentionStatus = null;
        if (unreadCount.mentionWeibo > 0 && ConfigManager.isNotificationMentionWeiboEnabled()) {
            List<WeiboStatus> mentionStatuses = fetchMentionStatuses(account);
            if (mentionStatuses.size() > 0) {
                mentionStatus = mentionStatuses.get(0);
            }
        }
        WeiboComment mentionComment = null;
        if (unreadCount.mentionComment > 0 && ConfigManager.isNotificationMentionCommentEnabled()) {
            List<WeiboComment> mentionComments = fetchMentionComments(account);
            if (mentionComments.size() > 0) {
                mentionComment = mentionComments.get(0);
            }
        }
        DirectMessage directMessage = null;
        if (unreadCount.directMessage > 0 && Utilities.isBmEnabled() && ConfigManager.isNotificationDmEnabled()) {
            directMessage = fetchDm(account);
        }
        if (comment != null) {
            Intent clickIntent = new Intent();
            clickIntent.setClass(GlobalContext.getInstance(), UnreadCommentReceiver.class);
            clickIntent.putExtra(MainActivity.UNREAD_PAGE_POSITION, MainActivity.COMMENT_LIST);
            clickIntent.putExtra(MainActivity.UNREAD_GROUP, CommentListFragment.ALL);
            clickIntent.putExtra(MainActivity.ACCOUNT_INDEX, GlobalContext.indexOfAccount(account));
            Intent intent = new Intent();
            intent.setClass(context, UnreadCommentNotificationService.class);
            intent.putExtra(AbsUnreadNotificationService.ACCOUNT, account);
            intent.putExtra(AbsUnreadNotificationService.MESSAGE, comment);
            intent.putExtra(AbsUnreadNotificationService.CLICK_INTENT, clickIntent);
            intent.putExtra(AbsUnreadNotificationService.COUNT, unreadCount.comment);
            context.startService(intent);
        }
        if (mentionStatus != null) {
            Intent clickIntent = new Intent();
            clickIntent.setClass(GlobalContext.getInstance(), UnreadMentionWeiboReceiver.class);
            clickIntent.putExtra(MainActivity.UNREAD_PAGE_POSITION, MainActivity.ATME_LIST);
            clickIntent.putExtra(MainActivity.UNREAD_GROUP, 0);
            clickIntent.putExtra(MainActivity.ACCOUNT_INDEX, GlobalContext.indexOfAccount(account));
            Intent intent = new Intent();
            intent.setClass(context, UnreadMentionWeiboNotificationService.class);
            intent.putExtra(AbsUnreadNotificationService.ACCOUNT, account);
            intent.putExtra(AbsUnreadNotificationService.MESSAGE, mentionStatus);
            intent.putExtra(AbsUnreadNotificationService.CLICK_INTENT, clickIntent);
            intent.putExtra(AbsUnreadNotificationService.COUNT, unreadCount.mentionWeibo);
            context.startService(intent);
        }
        if (mentionComment != null) {
            Intent clickIntent = new Intent();
            clickIntent.setClass(GlobalContext.getInstance(), UnreadMentionCommentReceiver.class);
            clickIntent.putExtra(MainActivity.UNREAD_PAGE_POSITION, MainActivity.COMMENT_LIST);
            clickIntent.putExtra(MainActivity.UNREAD_GROUP, CommentListFragment.ATME);
            clickIntent.putExtra(MainActivity.ACCOUNT_INDEX, GlobalContext.indexOfAccount(account));
            Intent intent = new Intent();
            intent.setClass(context, UnreadMentionCommentNotificationService.class);
            intent.putExtra(AbsUnreadNotificationService.ACCOUNT, account);
            intent.putExtra(AbsUnreadNotificationService.MESSAGE, mentionComment);
            intent.putExtra(AbsUnreadNotificationService.CLICK_INTENT, clickIntent);
            intent.putExtra(AbsUnreadNotificationService.COUNT, unreadCount.mentionComment);
            context.startService(intent);
        }
        if (directMessage != null) {
            // TODO
        }
    }
}
