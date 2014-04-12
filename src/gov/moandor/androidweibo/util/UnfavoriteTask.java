package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.UnfavoriteDao;

public class UnfavoriteTask extends MyAsyncTask<Void, Void, WeiboStatus> {
    private static final int CODE_NOT_FAVORITED = 20705;
    
    private WeiboStatus mStatus;
    private OnUnfavoriteFinishedListener mListener;
    private String mToken;
    private WeiboException mException;
    
    public UnfavoriteTask(WeiboStatus status, OnUnfavoriteFinishedListener l) {
        mStatus = status;
        mListener = l;
    }
    
    @Override
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
    }
    
    @Override
    protected WeiboStatus doInBackground(Void... v) {
        UnfavoriteDao dao = new UnfavoriteDao();
        dao.setToken(mToken);
        dao.setId(mStatus.id);
        try {
            return dao.execute();
        } catch (WeiboException e) {
            Logger.logExcpetion(e);
            mException = e;
        }
        cancel(true);
        return null;
    }
    
    @Override
    protected void onPostExecute(WeiboStatus result) {
        mListener.onUnfavoriteFinished(result);
    }
    
    @Override
    protected void onCancelled() {
        if (mException.getCode() == CODE_NOT_FAVORITED) {
            mStatus.favorited = false;
            mListener.onUnfavoriteFinished(mStatus);
        } else {
            mListener.onUnfavoriteFailed(mException);
        }
    }
    
    public static interface OnUnfavoriteFinishedListener {
        public void onUnfavoriteFinished(WeiboStatus status);
        
        public void onUnfavoriteFailed(WeiboException e);
    }
}
