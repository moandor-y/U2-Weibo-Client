package gov.moandor.androidweibo.dao;

public class CommentsMentionsDao extends WeiboCommentDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_MENTIONS;
    }
}
