package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.DeleteWeiboDao;

public class DeleteWeiboTask extends MyAsyncTask<Void, Void, Void> {
    private static final int CODE_ALREADY_DELETED = 20101;
    
    private String mToken;
    private OnDeleteFinishedListener mListener;
    private WeiboException mException;
    private long mId;
    
    public DeleteWeiboTask(long id, OnDeleteFinishedListener l) {
        mId = id;
        mListener = l;
    }
    
    @Override
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
    }
    
    @Override
    protected Void doInBackground(Void... v) {
        DeleteWeiboDao dao = new DeleteWeiboDao();
        dao.setToken(mToken);
        dao.setId(mId);
        try {
            dao.execute();
        } catch (WeiboException e) {
            Logger.logException(e);
            if (e.getCode() != CODE_ALREADY_DELETED) {
                mException = e;
                cancel(true);
            }
        }
        return null;
    }
    
    @Override
    protected void onCancelled() {
        mListener.onDeleteFailed(mException);
    }
    
    @Override
    protected void onPostExecute(Void result) {
        mListener.onDeleteFinished();
    }
    
    public static interface OnDeleteFinishedListener {
        public void onDeleteFinished();
        
        public void onDeleteFailed(WeiboException e);
    }
}
