package gov.moandor.androidweibo.dao;

public class BilateralTimelineDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_BILATERAL_TIMELINE;
    }
}
