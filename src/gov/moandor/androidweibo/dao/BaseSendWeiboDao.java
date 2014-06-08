package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.GpsLocation;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseSendWeiboDao<T> extends BaseHttpDao<T> {
    private String mToken;
    private String mStatus;
    private GpsLocation mLocation;
    
    @Override
    public T execute() throws WeiboException {
        HttpParams params = new HttpParams();
        initParams(params);
        HttpUtils.Method method = HttpUtils.Method.POST;
        return getResult(params, method);
    }
    
    protected void initParams(HttpParams params) {
        params.put("access_token", mToken);
        params.put("status", mStatus);
        if (mLocation != null) {
            params.put("lat", mLocation.latitude);
            params.put("long", mLocation.longitude);
        }
    }
    
    protected T getResult(HttpParams params, HttpUtils.Method method) throws WeiboException {
        HttpUtils.executeNormalTask(method, mUrl, params);
        return null;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setStatus(String status) {
        mStatus = status;
    }
    
    public void setLocation(GpsLocation location) {
        mLocation = location;
    }
}
