package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.HttpUtils.Method;
import gov.moandor.androidweibo.util.WeiboException;

public class UploadWeiboDao extends BaseSendWeiboDao<Boolean> {
    private String mPicPath;
    private HttpUtils.UploadListener mUploadListener;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_UPLOAD;
    }
    
    @Override
    protected Boolean getResult(HttpParams params, Method method) throws WeiboException {
        return HttpUtils.executeUploadTask(mUrl, params, mPicPath, "pic", mUploadListener);
    }
    
    public void setPicPath(String picPath) {
        mPicPath = picPath;
    }
    
    public void setUploadListener(HttpUtils.UploadListener l) {
        mUploadListener = l;
    }
}
