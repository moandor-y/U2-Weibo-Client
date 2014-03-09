package gov.moandor.androidweibo.fragment;

import gov.moandor.androidweibo.util.HttpUtils;

public class FollowerListFragment extends AbsFriendsUserListFragment {
    @Override
    String getUrl() {
        return HttpUtils.UrlHelper.FRIENDSHIPS_FOLLOWERS;
    }
}
