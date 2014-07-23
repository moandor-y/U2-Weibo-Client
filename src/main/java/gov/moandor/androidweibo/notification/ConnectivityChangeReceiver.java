package gov.moandor.androidweibo.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.Utilities;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final int REQUEST_CODE = 0;

    public static void judgeAlarm(Context context) {
        if (isConnected(context) && ConfigManager.isNotificationEnabled()) {
            startAlarm(context);
        } else {
            stopAlarm(context);
        }
    }

    private static void startAlarm(Context context) {
        long time = Utilities.getNotificationInterval();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(context, FetchUnreadMessageService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, time, pendingIntent);
    }

    private static void stopAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(context, FetchUnreadMessageService.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        judgeAlarm(context);
    }
}
