package gov.moandor.androidweibo.dao;

public class UpdateWeiboDao extends BaseSendWeiboDao<Void> {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_UPDATE;
    }
}
