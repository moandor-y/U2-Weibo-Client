package gov.moandor.androidweibo.dao;

public class BilateralTimelineDao extends BaseWeiboStatusTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_BILATERAL_TIMELINE;
    }
}
