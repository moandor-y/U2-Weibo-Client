package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.UnfollowDao;

public class UnfollowTask extends MyAsyncTask<Void, Void, WeiboUser> {
    private static final int CODE_ALREADY_UNFOLLOWED = 20522;

    private WeiboUser mUser;
    private OnUnfollowFinishedListener mListener;
    private WeiboException mException;
    private String mToken;

    public UnfollowTask(WeiboUser user, OnUnfollowFinishedListener l) {
        mUser = user;
        mListener = l;
    }

    @Override
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
    }

    @Override
    protected WeiboUser doInBackground(Void... v) {
        UnfollowDao dao = new UnfollowDao();
        dao.setToken(mToken);
        dao.setUid(mUser.id);
        try {
            return dao.execute();
        } catch (WeiboException e) {
            if (e.getCode() == CODE_ALREADY_UNFOLLOWED) {
                mUser.following = false;
                return mUser;
            } else {
                Logger.logException(e);
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
