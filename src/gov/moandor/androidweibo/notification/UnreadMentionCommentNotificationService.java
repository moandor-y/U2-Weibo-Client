package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboComment;

public class UnreadMentionCommentNotificationService extends AbsUnreadNotificationService<WeiboComment> {
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_mention_comments, count);
    }
}
