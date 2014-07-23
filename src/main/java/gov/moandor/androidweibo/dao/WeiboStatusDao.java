package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class WeiboStatusDao extends BaseHttpDao<WeiboStatus> {
    private String mToken;
    private long mId;

    @Override
    public WeiboStatus execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("id", mId);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getWeiboStatusFromJson(response);
    }

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_SHOW;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setId(long id) {
        mId = id;
    }
}
