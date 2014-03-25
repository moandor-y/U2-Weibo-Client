package gov.moandor.androidweibo.dao;

public class FriendsTimelineDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_FRIENDS_TIMELINE;
    }
}
