package gov.moandor.androidweibo.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseTimelineJsonDao<T extends AbsItemBean> extends BaseHttpDao<List<T>> {
    private String mToken;
    private T mSinceMessage;
    private long mSinceId;
    private long mMaxId;
    private int mCount = 20;
    private boolean mNoEnoughNewMessages;
    private boolean mDataFetched;

    @Override
    public List<T> execute() throws WeiboException {
        HttpParams params = new HttpParams();
        addParams(params);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        List<T> result = new ArrayList<>(parceJson(response));
        if (mSinceMessage != null && result.size() >= 1) {
            T earliestMessage = result.get(result.size() - 1);
            if (mSinceMessage.id == earliestMessage.id) {
                mNoEnoughNewMessages = true;
            }
            result.remove(earliestMessage);
        }
        mDataFetched = true;
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
        checkDataFetched();
        return mNoEnoughNewMessages;
    }

    protected void addParams(HttpParams params) {
        params.put("access_token", mToken);
        if (mSinceMessage != null) {
            params.put("since_id", mSinceMessage.id - 1);
        } else {
            params.put("since_id", mSinceId);
        }
        params.put("max_id", mMaxId);
        params.put("count", mCount);
    }

    protected void checkDataFetched() {
        if (!mDataFetched) {
            throw new IllegalStateException("You must call execute() before call " +
                    "noEnoughNewMessages().");
        }
    }

    protected abstract List<T> parceJson(String json) throws WeiboException;
}
