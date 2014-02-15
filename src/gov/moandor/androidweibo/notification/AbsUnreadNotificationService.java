package gov.moandor.androidweibo.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public abstract class AbsUnreadNotificationService<T extends AbsItemBean> extends Service {
    private static final long[] VIBRATE_PATTERN = {0, 200, 300, 200, 300};
    public static final String ACCOUNT;
    public static final String MESSAGE;
    public static final String CLICK_INTENT;
    public static final String COUNT;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        ACCOUNT = packageName + ".account";
        MESSAGE = packageName + ".message";
        CLICK_INTENT = packageName + ".click.intent";
        COUNT = packageName + ".count";
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
        builder.setContentText(mAccount.name);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setAutoCancel(true);
        builder.setContentIntent(getClickPendingIntent(mClickIntent));
        builder.setOnlyAlertOnce(true);
        if (mCount > 1) {
            builder.setNumber(mCount);
        }
        if (GlobalContext.isNotificationVibrateEnabled()) {
            builder.setVibrate(VIBRATE_PATTERN);
        }
        if (GlobalContext.isNotificationLedEnabled()) {
            builder.setLights(Color.WHITE, 2000, 2000);
        }
        Uri uri = null;
        String ringtone = GlobalContext.getNotificationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
            uri = Uri.parse(ringtone);
        }
        if (uri != null) {
            builder.setSound(uri);
        }
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(title);
        style.bigText(mMessage.text);
        style.setSummaryText(mAccount.name);
        builder.setStyle(style);
        return builder.build();
    }
    
    private PendingIntent getClickPendingIntent(Intent clickIntent) {
        return PendingIntent.getBroadcast(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    abstract String getTextTitle(int count);
}
