package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.concurrency.MyAsyncTask;

public class DeleteCommentTask extends MyAsyncTask<Void, Void, Void> {
    private static final int CODE_ALREADY_DELETED = 20201;
    
    private String mToken;
    private OnDeleteFinishedListener mListener;
    private WeiboException mException;
    private long mId;
    
    public DeleteCommentTask(long id, OnDeleteFinishedListener l) {
        mId = id;
        mListener = l;
    }
    
    @Override
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
    }
    
    @Override
    protected Void doInBackground(Void... v) {
        String url = HttpUtils.UrlHelper.COMMENTS_DESTROY;
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("cid", String.valueOf(mId));
        try {
            HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
        } catch (WeiboException e) {
            Logger.logExcpetion(e);
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
