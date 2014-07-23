package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class DmConversationDao extends BaseTimelineJsonDao<DirectMessage> {
    private long mUid;

    @Override
    protected String getUrl() {
        return UrlHelper.DIRECT_MESSAGES_CONVERSATION;
    }

    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("uid", mUid);
    }

    @Override
    protected List<DirectMessage> parceJson(String json) throws WeiboException {
        return JsonUtils.getDmsFromJson(json);
    }

    public void setUid(long uid) {
        mUid = uid;
    }
}
