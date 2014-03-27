package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class FavoritesDao extends WeiboStatusDao {
    private int mPage;
    
    @Override
    protected String getUrl() {
        return UrlHelper.FAVORITES;
    }
    
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return JsonUtils.getFavoritesFromJson(json);
    }
    
    @Override
    protected void addParams(HttpParams params) {
        super.addParams(params);
        params.putParam("page", mPage);
    }
    
    public void setPage(int page) {
        mPage = page;
    }
}
