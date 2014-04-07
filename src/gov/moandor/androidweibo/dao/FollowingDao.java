package gov.moandor.androidweibo.dao;

public class FollowingDao extends BaseFriendsUserListDao {
    @Override
    protected String getUrl() {
        return UrlHelper.FRIENDSHIPS_FRIENDS;
    }
}
