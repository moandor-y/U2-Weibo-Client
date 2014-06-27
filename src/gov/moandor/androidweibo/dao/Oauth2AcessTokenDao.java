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
    private String mUsername;
    private String mPassword;
    private String mClientId;
    private String mClientSecret;
    private String mGrantType;
    
    @Override
    public String execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("username", mUsername);
        params.put("password", mPassword);
        params.put("client_id", mClientId);
        params.put("client_secret", mClientSecret);
        params.put("grant_type", mGrantType);
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
    
    public void setUsername(String username) {
        mUsername = username;
    }
    
    public void setPassword(String password) {
        mPassword = password;
    }
    
    public void setClientId(String clientId) {
        mClientId = clientId;
    }
    
    public void setClientSecret(String clientSecret) {
        mClientSecret = clientSecret;
    }
    
    public void setGrantType(String grantType) {
        mGrantType = grantType;
    }
}
