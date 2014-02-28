package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboComment;

public class UnreadMentionCommentNotificationService extends AbsUnreadNotificationService<WeiboComment> {
    static final String COUNT_TYPE = "mention_cmt";
    
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_mention_comments, count);
    }
    
    @Override
    String getCountType() {
        return COUNT_TYPE;
    }
}
