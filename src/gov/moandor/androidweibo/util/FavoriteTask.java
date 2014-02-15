package gov.moandor.androidweibo.util;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

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
        String url = HttpUtils.UrlHelper.FAVORITES_CREATE;
        HttpParams params = new HttpParams();
        params.addParam("access_token", mToken);
        params.addParam("id", String.valueOf(mStatus.id));
        try {
            String response = HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
            JSONObject json = new JSONObject(response);
            return Utilities.getWeiboStatusFromJson(json.getJSONObject("status"));
        } catch (WeiboException e) {
            Logger.logExcpetion(e);
            mException = e;
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            mException = new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
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
