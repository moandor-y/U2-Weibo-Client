package gov.moandor.androidweibo.dao;

public class CommentsShowDao extends WeiboCommentDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_SHOW;
    }
}
