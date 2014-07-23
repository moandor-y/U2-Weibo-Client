package gov.moandor.androidweibo.notification;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;

public class UnreadDmNotificationService extends AbsUnreadNotificationService<DirectMessage> {
    static final String COUNT_TYPE = "dm";

    @Override
    String getTextTitle(int count) {
        return getString(R.string.new_dms, count);
    }

    @Override
    String getCountType() {
        return COUNT_TYPE;
    }
}
