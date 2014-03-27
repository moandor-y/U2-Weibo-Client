package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

public class UnfollowTask extends MyAsyncTask<Void, Void, WeiboUser> {
    private static final int CODE_ALREADY_UNFOLLOWED = 20522;
    
    private WeiboUser mUser;
    private OnUnfollowFinishedListener mListener;
    private WeiboException mException;
    
    public UnfollowTask(WeiboUser user, OnUnfollowFinishedListener l) {
        mUser = user;
        mListener = l;
    }
    
    @Override
    protected WeiboUser doInBackground(Void... v) {
        String url = HttpUtils.UrlHelper.FRIENDSHIPS_DESTROY;
        HttpParams params = new HttpParams();
        params.putParam("access_token", GlobalContext.getCurrentAccount().token);
        params.putParam("uid", mUser.id);
        try {
            String response = HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
            return JsonUtils.getWeiboUserFromJson(response);
        } catch (WeiboException e) {
            if (e.getCode() == CODE_ALREADY_UNFOLLOWED) {
                mUser.following = false;
                return mUser;
            } else {
                Logger.logExcpetion(e);
                mException = e;
            }
        }
        cancel(true);
        return null;
    }
    
    @Override
    protected void onPostExecute(WeiboUser result) {
        mListener.onUnfollowFinished(result);
    }
    
    @Override
    protected void onCancelled() {
        mListener.onUnfollowFailed(mException);
    }
    
    public static interface OnUnfollowFinishedListener {
        public void onUnfollowFinished(WeiboUser user);
        
        public void onUnfollowFailed(WeiboException e);
    }
}
