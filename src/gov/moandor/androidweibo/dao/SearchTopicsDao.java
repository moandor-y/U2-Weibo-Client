package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.HttpParams;

public class SearchTopicsDao extends WeiboStatusDao {
    private String mTopic;
    
    @Override
    protected String getUrl() {
        return UrlHelper.SEARCH_TOPICS;
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("q", mTopic);
    }
    
    public void setTopic(String topic) {
        mTopic = topic;
    }
}
