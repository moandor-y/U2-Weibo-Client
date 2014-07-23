package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class CommentsShowDao extends BaseWeiboCommentTimelineDao {
    private long mWeiboId;

    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_SHOW;
    }

    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("id", mWeiboId);
    }

    public void setWeiboId(long weiboId) {
        mWeiboId = weiboId;
    }
}
