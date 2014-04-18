package gov.moandor.androidweibo.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.ResetUnreadCountDao;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class AbsUnreadNotificationService<T extends AbsItemBean> extends Service {
    private static final long[] VIBRATE_PATTERN = {0, 200, 300, 200, 300};
    public static final String ACCOUNT;
    public static final String MESSAGE;
    public static final String CLICK_INTENT;
    public static final String COUNT;
    private static final String CLEAR_NOTIFICATION;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        ACCOUNT = packageName + ".ACCOUNT";
        MESSAGE = packageName + ".MESSAGE";
        CLICK_INTENT = packageName + ".CLICK_INTENT";
        COUNT = packageName + ".COUNT";
        CLEAR_NOTIFICATION = packageName + ".CLEAR_NOTIFICATION";
    }
    
    private Account mAccount;
    private T mMessage;
    private Intent mClickIntent;
    private int mCount;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAccount = intent.getParcelableExtra(ACCOUNT);
        mMessage = intent.getParcelableExtra(MESSAGE);
        mClickIntent = intent.getParcelableExtra(CLICK_INTENT);
        mCount = intent.getIntExtra(COUNT, 0);
        Notification notification = buildNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Long.valueOf(mMessage.id).intValue(), notification);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
    
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        String title = getTextTitle(mCount);
        builder.setTicker(title);
        builder.setContentTitle(title);
        builder.setContentText(mAccount.user.name);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);
        builder.setContentIntent(getClickPendingIntent(mClickIntent));
        builder.setOnlyAlertOnce(true);
        if (mCount > 1) {
            builder.setNumber(mCount);
        }
        if (ConfigManager.isNotificationVibrateEnabled()) {
            builder.setVibrate(VIBRATE_PATTERN);
        }
        if (ConfigManager.isNotificationLedEnabled()) {
            builder.setLights(Color.WHITE, 2000, 2000);
        }
        Uri uri = null;
        String ringtone = ConfigManager.getNotificationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
            uri = Uri.parse(ringtone);
        }
        if (uri != null) {
            builder.setSound(uri);
        }
        builder.setDeleteIntent(getDeletePendingIntent());
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(title);
        style.bigText(mMessage.text);
        style.setSummaryText(mAccount.user.name);
        builder.setStyle(style);
        return builder.build();
    }
    
    private PendingIntent getClickPendingIntent(Intent clickIntent) {
        return PendingIntent.getBroadcast(getBaseContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private PendingIntent getDeletePendingIntent() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CLEAR_NOTIFICATION);
        Utilities.registerReceiver(mClearNotificationReceiver, filter);
        Intent clearIntent = new Intent();
        clearIntent.setAction(CLEAR_NOTIFICATION);
        return PendingIntent.getBroadcast(getBaseContext(), 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    static void clearUnreadCount(final String token, final String countType) {
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResetUnreadCountDao dao = new ResetUnreadCountDao();
                dao.setToken(token);
                dao.setCountType(countType);
                try {
                    dao.execute();
                } catch (WeiboException e) {
                    Logger.logExcpetion(e);
                }
            }
        });
    }
    
    private BroadcastReceiver mClearNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utilities.unregisterReceiver(this);
            clearUnreadCount(mAccount.token, getCountType());
        }
    };
    
    abstract String getTextTitle(int count);
    
    abstract String getCountType();
}
