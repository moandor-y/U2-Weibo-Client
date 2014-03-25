package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseTimelineJsonDao<T extends AbsItemBean> extends BaseDataSetJsonDao<T> {
    private String mToken;
    private long mSinceId;
    private long mMaxId;
    private int mCount = 20;
    
    @Override
    public T[] getData() throws WeiboException {
        HttpParams params = new HttpParams();
        params.addParam("access_token", mToken);
        params.addParam("since_id", String.valueOf(mSinceId));
        params.addParam("max_id", String.valueOf(mMaxId));
        params.addParam("count", String.valueOf(mCount));
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return parceJson(response);
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setSinceId(long sinceId) {
        mSinceId = sinceId;
    }
    
    public void setMaxId(long maxId) {
        mMaxId = maxId;
    }
    
    public void setCount(int count) {
        mCount = count;
    }
    
    protected abstract T[] parceJson(String json) throws WeiboException;
}
