package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class CommentsToMeDao extends BaseWeiboCommentTimelineDao {
    private int mFilter;
    
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_TO_ME;
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
