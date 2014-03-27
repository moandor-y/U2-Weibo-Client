package gov.moandor.androidweibo.util;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

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
        String url = HttpUtils.UrlHelper.FAVORITES_DESTROY;
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("id", mStatus.id);
        try {
            String response = HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
            JSONObject json = new JSONObject(response);
            return JsonUtils.getWeiboStatusFromJson(json.getJSONObject("status"));
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
