package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class SendDmDao extends BaseHttpDao<DirectMessage> {
    private String mToken;
    private String mText;
    private long mUid;
    private String mScreenName;

    @Override
    public DirectMessage execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("text", mText);
        params.put("uid", mUid);
        params.put("screen_name", mScreenName);
        HttpUtils.Method method = HttpUtils.Method.POST;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getDmFromJson(response);
    }

    @Override
    protected String getUrl() {
        return UrlHelper.DIRECT_MESSAGES_NEW;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setUid(long uid) {
        mUid = uid;
    }

    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }
}
