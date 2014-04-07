package gov.moandor.androidweibo.dao;

import org.json.JSONObject;

import java.util.List;

import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

public class DirectMessagesUserListDao extends BaseUserListDao<DirectMessagesUser> {
    @Override
    protected List<DirectMessagesUser> getDataFromJson(JSONObject json) throws WeiboException {
        return JsonUtils.getDmUsersFromJson(json);
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.DIRECT_MESSAGES_USER_LIST;
    }
}
