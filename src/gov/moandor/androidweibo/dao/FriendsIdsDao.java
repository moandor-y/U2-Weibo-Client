package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.UserIds;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class FriendsIdsDao extends BaseHttpDao<List<Long>> {
    private String mToken;
    private long mUserId;
    private int mCount = 500;
    private int mCursor;
    private int mNextCursor;
    
    @Override
    public List<Long> execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("uid", mUserId);
        params.putParam("count", mCount);
        params.putParam("cursor", mCursor);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        UserIds userIds = JsonUtils.getUserIdsFromJson(response);
        mNextCursor = userIds.nextCursor;
        return userIds.ids;
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_FRIENDS_IDS;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setUserId(long userId) {
        mUserId = userId;
    }
    
    public void setCount(int count) {
        mCount = count;
    }
    
    public void setCursor(int cursor) {
        mCursor = cursor;
    }
    
    public int getNextCursor() {
        return mNextCursor;
    }
}
