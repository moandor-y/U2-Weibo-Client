package gov.moandor.androidweibo.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gov.moandor.androidweibo.activity.MainActivity;

public class UnreadCommentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
