package gov.moandor.androidweibo.dao;

public class CommentsMentionsDao extends BaseWeiboCommentTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_MENTIONS;
    }
}
