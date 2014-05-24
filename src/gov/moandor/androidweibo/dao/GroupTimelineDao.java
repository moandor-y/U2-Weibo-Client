package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class GroupTimelineDao extends FriendsTimelineDao {
    private long mListId;
    
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_GROUPS_TIMELINE;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("list_id", mListId);
    }
    
    public void setListId(long listId) {
        mListId = listId;
    }
}
