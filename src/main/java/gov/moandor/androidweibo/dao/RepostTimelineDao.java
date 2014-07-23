package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class RepostTimelineDao extends BaseWeiboStatusTimelineDao {
    private long mWeiboId;

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_REPOST_TIMELINE;
    }

    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("id", mWeiboId);
    }

    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return JsonUtils.getWeiboRepostsFromJson(json);
    }

    public void setWeiboId(long weiboId) {
        mWeiboId = weiboId;
    }
}
