package gov.moandor.androidweibo.util;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.FollowDao;

public class FollowTask extends MyAsyncTask<Void, Void, WeiboUser> {
    private static final int CODE_ALREADY_FOLLOWED = 20506;

    private WeiboUser mUser;
    private OnFollowFinishedListener mListener;
    private WeiboException mException;
    private String mToken;

    public FollowTask(WeiboUser user, OnFollowFinishedListener l) {
        mUser = user;
        mListener = l;
    }

    @Override
    protected WeiboUser doInBackground(Void... v) {
        FollowDao dao = new FollowDao();
        dao.setToken(mToken);
        dao.setUid(mUser.id);
        try {
            WeiboUser result = dao.execute();
            result.following = true;
            return result;
        } catch (WeiboException e) {
            if (e.getCode() == CODE_ALREADY_FOLLOWED) {
                mUser.following = true;
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
    protected void onPreExecute() {
        mToken = GlobalContext.getCurrentAccount().token;
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
