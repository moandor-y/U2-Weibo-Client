package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

public class UnfollowDao extends BaseHttpDao<WeiboUser> {
    private String mToken;
    private long mUid;
    
    @Override
    public WeiboUser execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("uid", mUid);
        HttpUtils.Method method = HttpUtils.Method.POST;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getWeiboUserFromJson(response);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_DESTROY;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setUid(long uid) {
        mUid = uid;
    }
}
