package gov.moandor.androidweibo.fragment;

import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.dao.BaseUserListDao;
import gov.moandor.androidweibo.dao.FollowersDao;

public class FollowerListFragment extends AbsFriendsUserListFragment {
    @Override
    protected BaseUserListDao<WeiboUser> onCreateDao() {
        return new FollowersDao();
    }
}
