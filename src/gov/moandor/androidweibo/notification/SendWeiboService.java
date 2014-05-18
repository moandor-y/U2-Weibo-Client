package gov.moandor.androidweibo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboDraft;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseSendWeiboDao;
import gov.moandor.androidweibo.dao.RepostWeiboDao;
import gov.moandor.androidweibo.dao.UpdateWeiboDao;
import gov.moandor.androidweibo.dao.UploadWeiboDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.Random;

public class SendWeiboService extends Service {
    public static final String TOKEN;
    public static final String WEIBO_DRAFT;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        TOKEN = packageName + ".TOKEN";
        WEIBO_DRAFT = packageName + ".WEIBO_DRAFT";
    }
    
    private NotificationManager mNotificationManager;
    private String mToken;
    private WeiboDraft mDraft;
    
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
        mDraft = intent.getParcelableExtra(WEIBO_DRAFT);
        new SendTask().execute();
        return START_REDELIVER_INTENT;
    }
    
    private PendingIntent getFailedClickIntent() {
        return PendingIntent.getActivity(getBaseContext(), 0, ActivityUtils.draftBoxActivity(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private class SendTask extends MyAsyncTask<Void, Integer, Void> {
        private static final int WAITING_RESPONSE = -1;
        private static final int COMPLETE = -2;
        
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
            builder.setProgress(0, 100, TextUtils.isEmpty(mDraft.picPath));
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            mNotificationId = new Random().nextInt(Integer.MAX_VALUE);
            mNotificationManager.notify(mNotificationId, builder.build());
        }
        
        @Override
        protected Void doInBackground(Void... v) {
            BaseSendWeiboDao<?> dao;
            if (TextUtils.isEmpty(mDraft.picPath)) {
                if (mDraft.retweetStatus == null) {
                    dao = new UpdateWeiboDao();
                } else {
                    RepostWeiboDao repostWeiboDao = new RepostWeiboDao();
                    repostWeiboDao.setId(mDraft.retweetStatus.id);
                    if (mDraft.commentWhenRepost && mDraft.commentOriWhenRepost) {
                        repostWeiboDao.setIsComment(3);
                    } else if (mDraft.commentWhenRepost) {
                        repostWeiboDao.setIsComment(1);
                    } else if (mDraft.commentOriWhenRepost) {
                        repostWeiboDao.setIsComment(2);
                    }
                    dao = repostWeiboDao;
                }
            } else {
                UploadWeiboDao uploadWeiboDao = new UploadWeiboDao();
                uploadWeiboDao.setPicPath(mDraft.picPath);
                uploadWeiboDao.setUploadListener(new UploadListener());
                dao = uploadWeiboDao;
            }
            dao.setToken(mToken);
            dao.setStatus(mDraft.content);
            dao.setLocation(mDraft.location);
            if (dao instanceof UploadWeiboDao) {
                try {
                    if (!((UploadWeiboDao) dao).execute()) {
                        mDraft.error = getString(R.string.upload_failed);
                        DatabaseUtils.insertDraft(mDraft);
                        cancel(true);
                    }
                } catch (WeiboException e) {
                    Logger.logExcpetion(e);
                    mDraft.error = e.getMessage();
                    DatabaseUtils.insertDraft(mDraft);
                    cancel(true);
                }
            } else {
                try {
                    dao.execute();
                } catch (WeiboException e) {
                    Logger.logExcpetion(e);
                    mDraft.error = e.getMessage();
                    DatabaseUtils.insertDraft(mDraft);
                    cancel(true);
                }
            }
            return null;
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
            builder.setTicker(getString(R.string.upload_picture));
            builder.setContentText(mDraft.content);
            builder.setOnlyAlertOnce(true);
            builder.setOngoing(true);
            builder.setSmallIcon(R.drawable.ic_upload);
            builder.setContentIntent(Utilities.newEmptyPendingIntent());
            switch (values[0]) {
            case WAITING_RESPONSE:
                builder.setContentTitle(getString(R.string.waiting_response));
                builder.setNumber(100);
                builder.setProgress(1, 1, false);
                break;
            case COMPLETE:
                break;
            default:
                builder.setContentTitle(getString(R.string.uploading));
                builder.setNumber(values[0]);
                builder.setProgress(values[1], values[0], false);
            }
            mNotificationManager.notify(mNotificationId, builder.build());
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
        
        private class UploadListener implements HttpUtils.UploadListener {
            private long mLastTime;
            
            @Override
            public void onTransferring(int sent, int total) {
                long time = SystemClock.uptimeMillis();
                if (time - mLastTime < 200) {
                    return;
                }
                mLastTime = time;
                double proportion = (double) sent / (double) total;
                publishProgress((int) (100 * proportion), 100);
            }
            
            @Override
            public void onWaitResponse() {
                publishProgress(WAITING_RESPONSE);
            }
            
            @Override
            public void onComplete() {
                publishProgress(COMPLETE);
            }
        }
    }
}
