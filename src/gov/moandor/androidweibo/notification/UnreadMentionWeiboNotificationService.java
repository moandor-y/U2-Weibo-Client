package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;

public class UnreadMentionWeiboNotificationService extends AbsUnreadNotificationService<WeiboStatus> {
    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_mention_weibos, count);
    }
}
