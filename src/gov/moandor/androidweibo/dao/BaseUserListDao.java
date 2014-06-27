package gov.moandor.androidweibo.dao;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class BaseUserListDao<T> extends BaseHttpDao<List<T>> {
    private String mToken;
    private int mCount;
    private int mCursor;
    private int mNextCursor;
    private int mTrimStatus;
    private long mUid;
    private boolean mDataFetched;
    
    @Override
    public List<T> execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("count", mCount);
        params.put("cursor", mCursor);
        params.put("uid", mUid);
        params.put("trim_status", mTrimStatus);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        JSONObject json;
        try {
            json = new JSONObject(response);
            List<T> result = getDataFromJson(json);
            mNextCursor = json.getInt("next_cursor");
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        } finally {
            mDataFetched = true;
        }
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setCount(int count) {
        mCount = count;
    }
    
    public void setCursor(int cursor) {
        mCursor = cursor;
    }
    
    public void setUid(long uid) {
        mUid = uid;
    }
    
    public void setTrimStatus(int trimStatus) {
        mTrimStatus = trimStatus;
    }
    
    public int getNextCursor() {
        if (!mDataFetched) {
            throw new IllegalStateException("You must call execute() before call getNextCursor().");
        }
        return mNextCursor;
    }
    
    protected abstract List<T> getDataFromJson(JSONObject json) throws WeiboException;
}
