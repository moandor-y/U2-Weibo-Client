package gov.moandor.androidweibo.dao;

public class MentionsTimelineDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_MENTIONS;
    }
}
