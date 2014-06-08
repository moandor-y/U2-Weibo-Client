package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class UnreadCountDao extends BaseHttpDao<UnreadCount> {
    private String mToken;
    
    @Override
    public UnreadCount execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getUnreadCountFromJson(response);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.REMIND_UNREAD_COUNT;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
}
