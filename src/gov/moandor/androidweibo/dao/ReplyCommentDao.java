package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class ReplyCommentDao extends BaseSendCommentDao {
    private long mCid;
    
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_REPLY;
    }
    
    @Override
    protected void initParams(HttpParams params) {
        super.initParams(params);
        params.put("cid", mCid);
    }
    
    public void setCid(long cid) {
        mCid = cid;
    }
}
