package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class CommentsMentionsDao extends BaseWeiboCommentTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_MENTIONS;
    }
}
