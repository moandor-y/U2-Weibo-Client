package gov.moandor.androidweibo.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.bean.UserIds;
import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboGeo;
import gov.moandor.androidweibo.bean.WeiboGroup;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;

public class JsonUtils {
    public static List<WeiboStatus> getWeiboStatusesFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray statuses = jsonStatuses.getJSONArray("statuses");
            int len = statuses.length();
            List<WeiboStatus> result = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                result.add(getWeiboStatusFromJson(statuses.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static List<WeiboStatus> getWeiboRepostsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray statuses = jsonStatuses.getJSONArray("reposts");
            int len = statuses.length();
            List<WeiboStatus> result = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                result.add(getWeiboStatusFromJson(statuses.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static List<WeiboStatus> getFavoritesFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray favorites = json.getJSONArray("favorites");
            int len = favorites.length();
            List<WeiboStatus> result = new ArrayList<WeiboStatus>();
            for (int i = 0; i < len; i++) {
                result.add(getWeiboStatusFromJson(favorites.getJSONObject(i).getJSONObject("status")));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static WeiboStatus getWeiboStatusFromJson(String jsonStr) throws WeiboException {
        try {
            return getWeiboStatusFromJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static WeiboStatus getWeiboStatusFromJson(JSONObject json) throws JSONException {
        WeiboStatus weiboStatus = new WeiboStatus();
        weiboStatus.createdAt = json.optString("created_at", null);
        weiboStatus.id = json.optLong("id");
        weiboStatus.mid = json.optLong("mid");
        weiboStatus.text = json.optString("text", null);
        weiboStatus.source = json.optString("source", null);
        weiboStatus.favorited = json.optBoolean("favorited");
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            weiboStatus.weiboUser = getWeiboUserFromJson(user);
        }
        weiboStatus.repostCount = json.optInt("reposts_count");
        weiboStatus.commentCount = json.optInt("comments_count");
        weiboStatus.attitudeCount = json.optInt("attitudes_count");
        JSONObject retweetedStatusObj = json.optJSONObject("retweeted_status");
        if (retweetedStatusObj != null) {
            weiboStatus.retweetStatus = getWeiboStatusFromJson(retweetedStatusObj);
        }
        JSONArray urls = json.optJSONArray("pic_urls");
        if (urls != null) {
            int count = urls.length();
            weiboStatus.picCount = count;
            weiboStatus.thumbnailPic = new String[count];
            weiboStatus.bmiddlePic = new String[count];
            weiboStatus.originalPic = new String[count];
            for (int i = 0; i < count; i++) {
                JSONObject url = urls.optJSONObject(i);
                weiboStatus.thumbnailPic[i] = url.optString("thumbnail_pic", null);
                weiboStatus.bmiddlePic[i] = weiboStatus.thumbnailPic[i].replace("thumbnail", "bmiddle");
                weiboStatus.originalPic[i] = weiboStatus.thumbnailPic[i].replace("thumbnail", "large");
            }
        }
        JSONObject geo = json.optJSONObject("geo");
        if (geo != null) {
            weiboStatus.weiboGeo = getWeiboGeoFromJson(geo);
        }
        return weiboStatus;
    }

    public static List<WeiboUser> getWeiboUsersFromJson(JSONObject json) throws WeiboException {
        try {
            JSONArray users = json.getJSONArray("users");
            int len = users.length();
            List<WeiboUser> weiboUsers = new ArrayList<WeiboUser>();
            for (int i = 0; i < len; i++) {
                weiboUsers.add(getWeiboUserFromJson(users.getJSONObject(i)));
            }
            return weiboUsers;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static WeiboUser getWeiboUserFromJson(String jsonStr) throws WeiboException {
        try {
            return getWeiboUserFromJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    private static WeiboUser getWeiboUserFromJson(JSONObject json) {
        WeiboUser result = new WeiboUser();
        result.id = json.optLong("id");
        result.name = json.optString("name", null);
        result.location = json.optString("location", null);
        result.description = json.optString("description", null);
        result.profileImageUrl = json.optString("profile_image_url", null);
        result.gender = json.optString("gender", null);
        result.followersCount = json.optInt("followers_count");
        result.friendsCount = json.optInt("friends_count");
        result.statusesCount = json.optInt("statuses_count");
        result.following = json.optBoolean("following");
        result.allowAllActMsg = json.optBoolean("allow_all_act_msg");
        result.verified = json.optBoolean("verified");
        result.remark = json.optString("remark", null);
        result.allowAllComment = json.optBoolean("allow_all_comment");
        result.avatarLargeUrl = json.optString("avatar_large", null);
        result.verifiedReason = json.optString("verified_reason", null);
        result.followMe = json.optBoolean("follow_me");
        result.onlineStatus = json.optInt("online_status");
        return result;
    }

    private static WeiboGeo getWeiboGeoFromJson(JSONObject json) throws JSONException {
        JSONArray array = json.optJSONArray("coordinates");
        if (array != null) {
            WeiboGeo weiboGeo = new WeiboGeo();
            weiboGeo.coordinate[0] = array.getDouble(0);
            weiboGeo.coordinate[1] = array.getDouble(1);
            return weiboGeo;
        } else {
            return null;
        }
    }

    public static long getWeiboAccountIdFromJson(String jsonStr) throws WeiboException {
        JSONObject json;
        try {
            json = new JSONObject(jsonStr);
            return json.getLong("uid");
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }

    }

    public static List<DirectMessagesUser> getDmUsersFromJson(JSONObject json) throws WeiboException {
        List<DirectMessagesUser> result = new ArrayList<DirectMessagesUser>();
        try {
            JSONArray userList = json.getJSONArray("user_list");
            int len = userList.length();
            for (int i = 0; i < len; i++) {
                JSONObject user = userList.getJSONObject(i);
                result.add(getDmUserFromJson(user));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    private static DirectMessagesUser getDmUserFromJson(JSONObject json) throws JSONException {
        WeiboUser user = getWeiboUserFromJson(json.getJSONObject("user"));
        DirectMessage dm = getDmFromJson(json.getJSONObject("direct_message"));
        DirectMessagesUser result = new DirectMessagesUser();
        result.message = dm;
        result.weiboUser = user;
        result.unreadCount = json.getInt("unread_count");
        return result;
    }

    public static DirectMessage getDmFromJson(String jsonStr) throws WeiboException {
        try {
            return getDmFromJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    private static DirectMessage getDmFromJson(JSONObject json) throws JSONException {
        DirectMessage result = new DirectMessage();
        result.id = json.getLong("id");
        result.createdAt = json.getString("created_at");
        result.text = json.getString("text");
        result.weiboUser = getWeiboUserFromJson(json.getJSONObject("sender"));
        result.recipient = getWeiboUserFromJson(json.getJSONObject("recipient"));
        return result;
    }

    public static List<WeiboComment> getWeiboCommentsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject jsonStatuses = new JSONObject(jsonStr);
            JSONArray comments = jsonStatuses.getJSONArray("comments");
            int len = comments.length();
            List<WeiboComment> weiboComments = new ArrayList<WeiboComment>();
            for (int i = 0; i < len; i++) {
                weiboComments.add(getWeiboCommentFromJson(comments.getJSONObject(i)));
            }
            return weiboComments;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    private static WeiboComment getWeiboCommentFromJson(JSONObject json) throws JSONException {
        WeiboComment weiboComment = new WeiboComment();
        weiboComment.createdAt = json.optString("created_at", null);
        weiboComment.id = json.optLong("id");
        weiboComment.text = json.optString("text", null);
        weiboComment.source = json.optString("source", null);
        weiboComment.mid = json.optLong("mid");
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            weiboComment.weiboUser = getWeiboUserFromJson(user);
        }
        JSONObject status = json.optJSONObject("status");
        if (status != null) {
            weiboComment.weiboStatus = getWeiboStatusFromJson(status);
        }
        JSONObject replied = json.optJSONObject("reply_comment");
        if (replied != null) {
            weiboComment.repliedComment = getWeiboCommentFromJson(replied);
        }
        return weiboComment;
    }

    public static UnreadCount getUnreadCountFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            UnreadCount result = new UnreadCount();
            result.weiboStatus = json.getInt("status");
            result.comment = json.getInt("cmt");
            result.mentionWeibo = json.getInt("mention_status");
            result.mentionComment = json.getInt("mention_cmt");
            result.directMessage = json.getInt("dm");
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static List<UserSuggestion> getUserSuggestionsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONArray json = new JSONArray(jsonStr);
            int len = json.length();
            List<UserSuggestion> result = new ArrayList<UserSuggestion>();
            for (int i = 0; i < len; i++) {
                UserSuggestion suggestion = new UserSuggestion();
                JSONObject jsonSuggestion = json.getJSONObject(i);
                suggestion.id = jsonSuggestion.getLong("uid");
                suggestion.nickname = jsonSuggestion.getString("nickname");
                suggestion.remark = jsonSuggestion.getString("remark");
                result.add(suggestion);
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static UserIds getUserIdsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            UserIds result = new UserIds();
            result.ids = new ArrayList<Long>();
            JSONArray ids = json.getJSONArray("ids");
            for (int i = 0; i < ids.length(); i++) {
                result.ids.add(ids.getLong(i));
            }
            result.nextCursor = json.getInt("next_cursor");
            result.previousCursor = json.getInt("previous_cursor");
            result.totalNumber = json.getInt("total_number");
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static List<DirectMessage> getDmsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray array = json.getJSONArray("direct_messages");
            List<DirectMessage> result = new ArrayList<DirectMessage>();
            for (int i = 0; i < array.length(); i++) {
                result.add(getDmFromJson(array.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    public static List<WeiboGroup> getGroupsFromJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray array = json.getJSONArray("lists");
            List<WeiboGroup> result = new ArrayList<WeiboGroup>();
            for (int i = 0; i < array.length(); i++) {
                result.add(getGroupFromJson(array.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            Logger.logException(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
    }

    private static WeiboGroup getGroupFromJson(JSONObject json) throws JSONException {
        WeiboGroup result = new WeiboGroup();
        result.id = json.getLong("id");
        result.name = json.getString("name");
        return result;
    }
}
