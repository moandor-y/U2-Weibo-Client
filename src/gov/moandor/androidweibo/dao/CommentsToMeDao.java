package gov.moandor.androidweibo.dao;

public class CommentsToMeDao extends WeiboCommentDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_TO_ME;
    }
}
