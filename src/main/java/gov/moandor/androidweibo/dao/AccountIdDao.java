package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class AccountIdDao extends BaseHttpDao<Long> {
    private String mToken;

    @Override
    public Long execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getWeiboAccountIdFromJson(response);
    }

    @Override
    protected String getUrl() {
        return UrlHelper.ACCOUNT_GET_UID;
    }

    public void setToken(String token) {
        mToken = token;
    }
}
