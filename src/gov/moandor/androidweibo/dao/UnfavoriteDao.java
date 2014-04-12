package gov.moandor.androidweibo.dao;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.WeiboException;

public class UnfavoriteDao extends BaseHttpDao<WeiboStatus> {
    private String mToken;
    private long mId;
    
    @Override
    public WeiboStatus execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("id", mId);
        HttpUtils.Method method = HttpUtils.Method.POST;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        try {
            JSONObject json = new JSONObject(response);
            return JsonUtils.getWeiboStatusFromJson(json.getJSONObject("status"));
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.FAVORITES_DESTROY;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setId(long id) {
        mId = id;
    }
}
