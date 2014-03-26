package gov.moandor.androidweibo.dao;

public class UserTimelineDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_USER_TIMELINE;
    }
}
