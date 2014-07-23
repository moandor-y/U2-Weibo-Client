package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class FavoritesDao extends BaseWeiboStatusTimelineDao {
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
        params.put("page", mPage);
    }

    public void setPage(int page) {
        mPage = page;
    }
}
