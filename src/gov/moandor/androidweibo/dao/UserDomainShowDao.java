package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

/**
 * Created by Moandor on 7/4/2014.
 */
public class UserDomainShowDao extends BaseHttpDao<WeiboUser> {
    private String mToken;
    private String mDomain;

    @Override
    public WeiboUser execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("domain", mDomain);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getWeiboUserFromJson(response);
    }

    @Override
    protected String getUrl() {
        return UrlHelper.USERS_DOMAIN_SHOW;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setDomain(String domain) {
        mDomain = domain;
    }
}
