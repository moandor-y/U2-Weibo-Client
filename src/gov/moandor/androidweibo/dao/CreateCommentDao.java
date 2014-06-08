package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class CreateCommentDao extends BaseSendCommentDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_CREATE;
    }
}
