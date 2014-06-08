package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseSendCommentDao extends BaseHttpDao<Void> {
    private String mToken;
    private String mComment;
    private long mId;
    private boolean mCommentOri;
    
    @Override
    public Void execute() throws WeiboException {
        HttpParams params = new HttpParams();
        initParams(params);
        HttpUtils.Method method = HttpUtils.Method.POST;
        HttpUtils.executeNormalTask(method, mUrl, params);
        return null;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setComment(String comment) {
        mComment = comment;
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public void setCommentOri(boolean commentOri) {
        mCommentOri = commentOri;
    }
    
    protected void initParams(HttpParams params) {
        params.put("access_token", mToken);
        params.put("comment", mComment);
        params.put("id", mId);
        if (mCommentOri) {
            params.put("comment_ori", 1);
        }
    }
}
