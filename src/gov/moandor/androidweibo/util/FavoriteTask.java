package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.FavoriteDao;

public class FavoriteTask extends MyAsyncTask<Void, Void, WeiboStatus> {
    private static final int CODE_ALREADY_FAVORITED = 20704;
    
    private WeiboStatus mStatus;
    private OnFavoriteFinishedListener mListener;
    private String mToken;
    private WeiboException mException;
    
    public FavoriteTask(WeiboStatus status, OnFavoriteFinishedListener l) {
        mStatus = status;
        mListener = l;
    }
    
    @Override
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
    }
    
    @Override
    protected WeiboStatus doInBackground(Void... v) {
        FavoriteDao dao = new FavoriteDao();
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
        mListener.onFavoriteFinished(result);
    }
    
    @Override
    protected void onCancelled() {
        if (mException.getCode() == CODE_ALREADY_FAVORITED) {
            mStatus.favorited = true;
            mListener.onFavoriteFinished(mStatus);
        } else {
            mListener.onFavoriteFailed(mException);
        }
    }
    
    public static interface OnFavoriteFinishedListener {
        public void onFavoriteFinished(WeiboStatus status);
        
        public void onFavoriteFailed(WeiboException e);
    }
}
