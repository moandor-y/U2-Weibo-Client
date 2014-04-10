package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class MentionsWeiboTimelineDao extends BaseWeiboStatusTimelineDao {
    private int mFilter;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_MENTIONS;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("filter_by_author", mFilter);
    }
    
    public void setFilter(int filter) {
        mFilter = filter;
    }
}
