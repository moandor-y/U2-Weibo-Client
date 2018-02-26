package gov.moandor.androidweibo.dao;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class Oauth2AcessTokenDao extends BaseHttpDao<String> {
    private String mCode;

    @Override
    public String execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("client_id", UrlHelper.APPKEY);
        params.put("client_secret", UrlHelper.APPSECRET);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", UrlHelper.AUTH_REDIRECT);
        params.put("code", mCode);
        HttpUtils.Method method = HttpUtils.Method.POST;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        JSONObject json;
        try {
            json = new JSONObject(response);
            return json.getString("access_token");
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    @Override
    protected String getUrl() {
        return UrlHelper.OAUTH2_ACCESS_TOKEN;
    }

    public void setCode(String code) {
        mCode = code;
    }
}
