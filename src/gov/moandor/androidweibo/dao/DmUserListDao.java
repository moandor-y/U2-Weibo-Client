package gov.moandor.androidweibo.dao;

import org.json.JSONObject;

import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class DmUserListDao extends BaseUserListDao<DirectMessagesUser> {
    @Override
    protected List<DirectMessagesUser> getDataFromJson(JSONObject json) throws WeiboException {
        return JsonUtils.getDmUsersFromJson(json);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.DIRECT_MESSAGES_USER_LIST;
    }
}
