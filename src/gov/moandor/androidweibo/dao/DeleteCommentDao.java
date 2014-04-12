package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

public class DeleteCommentDao extends BaseHttpDao<Void> {
    private String mToken;
    private long mCid;
    
    @Override
    public Void execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("cid", mCid);
        HttpUtils.Method method = HttpUtils.Method.POST;
        HttpUtils.executeNormalTask(method, mUrl, params);
        return null;
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.COMMENTS_DESTROY;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setCid(long cid) {
        mCid = cid;
    }
}
