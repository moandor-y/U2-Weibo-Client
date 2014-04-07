package gov.moandor.androidweibo.dao;

public class CommentsByMeDao extends BaseWeiboCommentTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_BY_ME;
    }
}
