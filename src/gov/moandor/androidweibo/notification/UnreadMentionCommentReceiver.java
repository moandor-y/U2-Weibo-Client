package gov.moandor.androidweibo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.util.GlobalContext;

public class UnreadMentionCommentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Account account = GlobalContext.getAccount(intent.getIntExtra(MainActivity.ACCOUNT_INDEX, 0));
        AbsUnreadNotificationService.clearUnreadCount(account.token, 
                UnreadMentionCommentNotificationService.COUNT_TYPE);
    }
}
