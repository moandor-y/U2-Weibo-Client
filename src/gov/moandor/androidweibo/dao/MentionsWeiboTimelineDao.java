package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class MentionsWeiboTimelineDao extends BaseWeiboStatusTimelineDao {
    private int mFilter;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_MENTIONS;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("filter_by_author", mFilter);
    }
    
    public void setFilter(int filter) {
        mFilter = filter;
    }
}
