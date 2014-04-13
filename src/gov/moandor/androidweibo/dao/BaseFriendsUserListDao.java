package gov.moandor.androidweibo.dao;

import org.json.JSONObject;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class BaseFriendsUserListDao extends BaseUserListDao<WeiboUser> {
    @Override
    protected List<WeiboUser> getDataFromJson(JSONObject json) throws WeiboException {
        return JsonUtils.getWeiboUsersFromJson(json);
    }
}
