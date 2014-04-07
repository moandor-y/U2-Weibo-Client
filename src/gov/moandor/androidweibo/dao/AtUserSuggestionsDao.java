package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class AtUserSuggestionsDao extends BaseDataSetJsonDao<UserSuggestion> {
    private String mKeyword;
    private String mToken;
    
    @Override
    public List<UserSuggestion> fetchData() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("q", mKeyword);
        params.putParam("type", "0");
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        return JsonUtils.getUserSuggestionsFromJson(response);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.SEARCH_SUGGESTIONS_AT_USERS;
    }
    
    public void setKeyword(String keyword) {
        mKeyword = keyword;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
}
