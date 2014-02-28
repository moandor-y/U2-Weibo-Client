package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;

public class UnreadMentionWeiboNotificationService extends AbsUnreadNotificationService<WeiboStatus> {
    static final String COUNT_TYPE = "mention_status";
    
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_mention_weibos, count);
    }
    
    @Override
    String getCountType() {
        return COUNT_TYPE;
    }
}
