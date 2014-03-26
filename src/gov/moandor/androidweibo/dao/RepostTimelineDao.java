package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class RepostTimelineDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_REPOST_TIMELINE;
    }
    
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return Utilities.getWeiboRepostsFromJson(json);
    }
}
