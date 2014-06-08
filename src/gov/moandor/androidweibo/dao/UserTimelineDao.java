package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class UserTimelineDao extends BaseWeiboStatusTimelineDao {
    private long mUserId;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_USER_TIMELINE;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("uid", mUserId);
    }
    
    public void setUserId(long userId) {
        mUserId = userId;
    }
}
