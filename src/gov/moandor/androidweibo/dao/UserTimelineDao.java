package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class UserTimelineDao extends WeiboStatusDao {
    private long mUserId;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_USER_TIMELINE;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("uid", String.valueOf(mUserId));
    }
    
    public void setUserId(long userId) {
        mUserId = userId;
    }
}
