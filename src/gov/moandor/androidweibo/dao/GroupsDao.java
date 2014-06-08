package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboGroup;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class GroupsDao extends BaseHttpDao<List<WeiboGroup>> {
    private String mToken;
    
    @Override
    public List<WeiboGroup> execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getGroupsFromJson(response);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_GROUPS;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
}
