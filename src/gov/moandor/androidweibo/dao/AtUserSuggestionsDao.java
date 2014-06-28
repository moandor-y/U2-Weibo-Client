package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

public class AtUserSuggestionsDao extends BaseHttpDao<List<UserSuggestion>> {
    private String mKeyword;
    private String mToken;

    @Override
    public List<UserSuggestion> execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.put("access_token", mToken);
        params.put("q", mKeyword);
        params.put("type", "0");
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
