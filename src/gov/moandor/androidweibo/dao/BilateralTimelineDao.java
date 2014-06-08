package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class BilateralTimelineDao extends BaseWeiboStatusTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_BILATERAL_TIMELINE;
    }
}
