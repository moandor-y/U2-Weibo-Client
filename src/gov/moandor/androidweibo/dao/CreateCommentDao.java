package gov.moandor.androidweibo.dao;

public class CreateCommentDao extends BaseSendCommentDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_CREATE;
    }
}
