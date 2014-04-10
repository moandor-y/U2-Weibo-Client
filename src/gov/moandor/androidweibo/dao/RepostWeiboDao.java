package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

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
        params.putParam("id", mId);
        params.putParam("is_comment", mIsComment);
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public void setIsComment(int isComment) {
        mIsComment = isComment;
    }
}
