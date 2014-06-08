package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.UrlHelper;

public class SearchTopicsDao extends BaseWeiboStatusTimelineDao {
    private String mTopic;
    
    @Override
    protected String getUrl() {
        return UrlHelper.SEARCH_TOPICS;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.put("q", mTopic);
    }
    
    public void setTopic(String topic) {
        mTopic = topic;
    }
}
