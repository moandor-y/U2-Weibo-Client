package gov.moandor.androidweibo.dao;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class WeiboMidToIdDao extends BaseHttpDao<Long> {
    private String mToken;
    private String mMid;

    @Override
    public Long execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("mid", mMid);
        params.put("type", "1");
        params.put("isBase62", "1");
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        try {
            JSONObject json = new JSONObject(response);
            return Long.valueOf(json.getString("id"));
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_QUERYID;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setMid(String mid) {
        mMid = mid;
    }
}
