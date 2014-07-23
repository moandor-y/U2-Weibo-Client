package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class CommentsByMeDao extends BaseWeiboCommentTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_BY_ME;
    }
}
