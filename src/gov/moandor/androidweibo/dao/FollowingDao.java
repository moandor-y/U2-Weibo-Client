package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.UrlHelper;

public class FollowingDao extends BaseFriendsUserListDao {
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_FRIENDS;
    }
}
