package gov.moandor.androidweibo.dao;

public class FollowersDao extends BaseFriendsUserListDao {
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_FOLLOWERS;
    }
}
