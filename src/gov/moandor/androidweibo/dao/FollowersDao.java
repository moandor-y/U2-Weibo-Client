package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class FollowersDao extends BaseFriendsUserListDao {
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_FOLLOWERS;
    }
}
