package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.WeiboException;

public class UserShowDao extends BaseHttpDao<WeiboUser> {
    private String mToken;
    private String mScreenName;
    private long mUid;
    
    @Override
    public WeiboUser execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        if (mUid != 0) {
            params.putParam("uid", mUid);
        } else if (!TextUtils.isEmpty(mScreenName)) {
            params.putParam("screen_name", mScreenName);
        }
        String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, mUrl, params);
        return JsonUtils.getWeiboUserFromJson(response);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.USERS_SHOW;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }
    
    public void setUid(long uid) {
        mUid = uid;
    }
}
