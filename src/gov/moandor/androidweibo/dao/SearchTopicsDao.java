package gov.moandor.androidweibo.dao;

public class SearchTopicsDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.SEARCH_TOPICS;
    }
}
