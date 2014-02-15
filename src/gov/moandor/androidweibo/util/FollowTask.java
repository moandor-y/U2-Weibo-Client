package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

public class FollowTask extends MyAsyncTask<Void, Void, WeiboUser> {
    private static final int CODE_ALREADY_FOLLOWED = 20506;
    
    private WeiboUser mUser;
    private OnFollowFinishedListener mListener;
    private WeiboException mException;
    
    public FollowTask(WeiboUser user, OnFollowFinishedListener l) {
        mUser = user;
        mListener = l;
    }
    
    @Override
    protected WeiboUser doInBackground(Void... v) {
        String url = HttpUtils.UrlHelper.FRIENDSHIPS_CREATE;
        HttpParams params = new HttpParams();
        params.addParam("access_token", GlobalContext.getCurrentAccount().token);
        params.addParam("uid", String.valueOf(mUser.id));
        try {
            String response = HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
            WeiboUser result = Utilities.getWeiboUserFromJson(response);
            result.following = true;
            return result;
        } catch (WeiboException e) {
            if (e.getCode() == CODE_ALREADY_FOLLOWED) {
                mUser.following = true;
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
        mListener.onFollowFinished(result);
    }
    
    @Override
    protected void onCancelled() {
        mListener.onFollowFailed(mException);
    }
    
    public static interface OnFollowFinishedListener {
        public void onFollowFinished(WeiboUser user);
        public void onFollowFailed(WeiboException e);
    }
}
