package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class RepostWeiboDao extends BaseSendWeiboDao<Void> {
    private long mId;
    private int mIsComment;

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_REPOST;
    }

    @Override
    protected void initParams(HttpParams params) {
        super.initParams(params);
        params.put("id", mId);
        params.put("is_comment", mIsComment);
    }

    public void setId(long id) {
        mId = id;
    }

    public void setIsComment(int isComment) {
        mIsComment = isComment;
    }
}
