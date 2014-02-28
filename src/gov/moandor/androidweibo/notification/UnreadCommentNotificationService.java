package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboComment;

public class UnreadCommentNotificationService extends AbsUnreadNotificationService<WeiboComment> {
    static final String COUNT_TYPE = "cmt";
    
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_comments, count);
    }
    
    @Override
    String getCountType() {
        return COUNT_TYPE;
    }
}
