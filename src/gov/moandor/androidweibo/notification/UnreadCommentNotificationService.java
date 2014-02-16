package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboComment;

public class UnreadCommentNotificationService extends AbsUnreadNotificationService<WeiboComment> {
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_comments, count);
    }
    
    @Override
    String getCountType() {
        return "cmt";
    }
}
