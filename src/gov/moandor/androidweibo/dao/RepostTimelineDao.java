package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class RepostTimelineDao extends BaseWeiboStatusTimelineDao {
    private long mWeiboId;
    
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_REPOST_TIMELINE;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("id", mWeiboId);
    }
    
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return JsonUtils.getWeiboRepostsFromJson(json);
    }
    
    public void setWeiboId(long weiboId) {
        mWeiboId = weiboId;
    }
}
