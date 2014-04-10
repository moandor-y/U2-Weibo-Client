package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class ReplyCommentDao extends BaseSendCommentDao {
    private long mCid;
    
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_REPLY;
    }
    
    @Override
    protected void initParams(HttpParams params) {
        super.initParams(params);
        params.putParam("cid", mCid);
    }
    
    public void setCid(long cid) {
        mCid = cid;
    }
}
