package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class DeleteWeiboDao extends BaseHttpDao<Void> {
    private String mToken;
    private long mId;

    @Override
    public Void execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("id", mId);
        HttpUtils.Method method = HttpUtils.Method.POST;
        HttpUtils.executeNormalTask(method, mUrl, params);
        return null;
    }

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_DESTROY;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setId(long id) {
        mId = id;
    }
}
