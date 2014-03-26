package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class FavoritesDao extends WeiboStatusDao {
    @Override
    protected String getUrl() {
        return UrlHelper.FAVORITES;
    }
    
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return Utilities.getFavoritesFromJson(json);
    }
}
