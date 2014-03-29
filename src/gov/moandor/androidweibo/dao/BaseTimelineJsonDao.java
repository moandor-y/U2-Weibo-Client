package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseTimelineJsonDao<T extends AbsItemBean> extends BaseDataSetJsonDao<T> {
    private String mToken;
    private T mSinceMessage;
    private long mSinceId;
    private long mMaxId;
    private int mCount = 20;
    private boolean mNoEnoughNewMessages;
    
    @Override
    public List<T> fetchData() throws WeiboException {
        HttpParams params = new HttpParams();
        addParams(params);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        List<T> result = new CopyOnWriteArrayList<T>(parceJson(response));
        if (mSinceMessage != null && result.size() >= 1) {
            T earliestMessage = result.get(result.size() - 1);
            if (mSinceMessage.id == earliestMessage.id) {
                mNoEnoughNewMessages = true;
            }
            result.remove(earliestMessage);
        }
        return result;
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
    
    public void setSinceMessage(T message) {
        mSinceMessage = message;
    }
    
    public boolean noEnoughNewMessages() {
        return mNoEnoughNewMessages;
    }
    
    protected void addParams(HttpParams params) {
        params.putParam("access_token", mToken);
        if (mSinceMessage != null) {
            params.putParam("since_id", mSinceMessage.id - 1);
        } else {
            params.putParam("since_id", mSinceId);
        }
        params.putParam("max_id", mMaxId);
        params.putParam("count", mCount);
    }
    
    protected abstract List<T> parceJson(String json) throws WeiboException;
}
