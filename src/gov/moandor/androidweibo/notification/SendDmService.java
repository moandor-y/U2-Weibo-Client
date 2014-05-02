package gov.moandor.androidweibo.notification;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.SendDmDao;
import gov.moandor.androidweibo.fragment.DmConversationFragment;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class SendDmService extends Service {
    public static final String TOKEN;
    public static final String TEXT;
    public static final String USER_ID;
    public static final String SCREEN_NAME;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        TOKEN = packageName + ".TOKEN";
        TEXT = packageName + ".TEXT";
        USER_ID = packageName + ".USER_ID";
        SCREEN_NAME = packageName + ".SCREEN_NAME";
    }
    
    private long mUserId;
    private NotificationManager mNotificationManager;
    private String mToken;
    private String mText;
    private String mScreenName;
    private String mError;
    
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mToken = intent.getStringExtra(TOKEN);
        mText = intent.getStringExtra(TEXT);
        mUserId = intent.getLongExtra(USER_ID, 0);
        mScreenName = intent.getStringExtra(SCREEN_NAME);
        new SendTask().execute();
        return START_REDELIVER_INTENT;
    }
    
    private class SendTask extends MyAsyncTask<Void, Void, DirectMessage> {
        private int mNotificationId;
        
        @Override
        protected void onPreExecute() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.sending));
            builder.setContentTitle(getString(R.string.sending));
            builder.setContentText(mText);
            builder.setOnlyAlertOnce(true);
            builder.setOngoing(true);
            builder.setSmallIcon(R.drawable.ic_upload);
            builder.setProgress(0, 100, true);
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            mNotificationId = new Random().nextInt(Integer.MAX_VALUE);
            mNotificationManager.notify(mNotificationId, builder.build());
        }
        
        @Override
        protected DirectMessage doInBackground(Void... v) {
            SendDmDao dao = new SendDmDao();
            dao.setToken(mToken);
            dao.setText(mText);
            dao.setUid(mUserId);
            dao.setScreenName(mScreenName);
            try {
                return dao.execute();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                mError = e.getMessage();
                cancel(true);
            }
            return null;
        }
        
        @Override
        protected void onCancelled() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.send_failed));
            builder.setContentTitle(getString(R.string.send_failed));
            builder.setContentText(mText);
            builder.setOnlyAlertOnce(true);
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_cancel);
            builder.setOngoing(false);
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            mNotificationManager.notify(mNotificationId, builder.build());
            GlobalContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(mNotificationId);
                    stopForeground(true);
                    stopSelf();
                }
            }, 3000);
            Intent intent = new Intent();
            intent.setAction(DmConversationFragment.SEND_FINISHED);
            intent.putExtra(DmConversationFragment.SEND_RESULT_CODE, DmConversationFragment.SEND_FAILED);
            intent.putExtra(DmConversationFragment.SEND_FAILED_TEXT, mText);
            intent.putExtra(DmConversationFragment.SEND_FAILED_ERROR, mError);
            sendBroadcast(intent);
        }
        
        @Override
        protected void onPostExecute(DirectMessage result) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.sent_successfully));
            builder.setContentTitle(getString(R.string.sent_successfully));
            builder.setOnlyAlertOnce(true);
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_accept);
            builder.setOngoing(false);
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            mNotificationManager.notify(mNotificationId, builder.build());
            GlobalContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(mNotificationId);
                    stopForeground(true);
                    stopSelf();
                }
            }, 3000);
            Intent intent = new Intent();
            intent.setAction(DmConversationFragment.SEND_FINISHED);
            intent.putExtra(DmConversationFragment.SEND_RESULT_CODE, DmConversationFragment.SEND_SUCCESSFUL);
            intent.putExtra(DmConversationFragment.SEND_SUCCESSFUL_MESSAGE, result);
            sendBroadcast(intent);
        }
    }
}
