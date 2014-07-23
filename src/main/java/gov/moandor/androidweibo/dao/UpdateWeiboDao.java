package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class UpdateWeiboDao extends BaseSendWeiboDao<Void> {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_UPDATE;
    }
}
