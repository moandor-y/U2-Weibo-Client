package gov.moandor.androidweibo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.DraftBoxActivity;
import gov.moandor.androidweibo.bean.CommentDraft;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.Random;

public class SendCommentService extends Service {
    public static final String TOKEN;
    public static final String COMMENT_DRAFT;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        TOKEN = packageName + ".TOKEN";
        COMMENT_DRAFT = packageName + ".COMMENT_DRAFT";
    }
    
    private NotificationManager mNotificationManager;
    private String mToken;
    private CommentDraft mDraft;
    
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
        mDraft = intent.getParcelableExtra(COMMENT_DRAFT);
        new SendTask().execute();
        return START_REDELIVER_INTENT;
    }
    
    private PendingIntent getFailedClickIntent() {
        Intent intent = new Intent();
        intent.setClass(getBaseContext(), DraftBoxActivity.class);
        return PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private class SendTask extends MyAsyncTask<Void, Integer, Void> {
        private int mNotificationId;
        
        @Override
        protected void onPreExecute() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.sending));
            builder.setContentTitle(getString(R.string.sending));
            builder.setContentText(mDraft.content);
            builder.setOnlyAlertOnce(true);
            builder.setOngoing(true);
            builder.setSmallIcon(R.drawable.ic_upload);
            builder.setProgress(0, 100, true);
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            mNotificationId = new Random().nextInt(Integer.MAX_VALUE);
            mNotificationManager.notify(mNotificationId, builder.build());
        }
        
        @Override
        protected Void doInBackground(Void... v) {
            HttpParams params = new HttpParams();
            params.addParam("access_token", mToken);
            params.addParam("comment", mDraft.content);
            params.addParam("id", String.valueOf(mDraft.commentedStatus.id));
            if (mDraft.commentOri) {
                params.addParam("comment_ori", "1");
            }
            String url;
            if (mDraft.repliedComment == null) {
                url = HttpUtils.UrlHelper.COMMENTS_CREATE;
            } else {
                params.addParam("cid", String.valueOf(mDraft.repliedComment.id));
                url = HttpUtils.UrlHelper.COMMENTS_REPLY;
            }
            try {
                HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                mDraft.error = e.getMessage();
                DatabaseUtils.insertDraft(mDraft);
                cancel(true);
            }
            return null;
        }
        
        @Override
        protected void onCancelled() {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.send_failed));
            builder.setContentTitle(getString(R.string.send_failed));
            builder.setContentText(mDraft.content);
            builder.setOnlyAlertOnce(true);
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_cancel);
            builder.setOngoing(false);
            builder.setContentIntent(getFailedClickIntent());
            mNotificationManager.notify(mNotificationId, builder.build());
            GlobalContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(mNotificationId);
                    stopForeground(true);
                    stopSelf();
                }
            }, 3000);
        }
        
        @Override
        protected void onPostExecute(Void result) {
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
        }
    }
}
