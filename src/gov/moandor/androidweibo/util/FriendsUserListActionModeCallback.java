package gov.moandor.androidweibo.util;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.FriendsUserListAdapter;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.AbsUserListFragment;

public class FriendsUserListActionModeCallback implements ActionMode.Callback {
    private AbsUserListFragment<FriendsUserListAdapter, WeiboUser> mFragment;
    private FriendsUserListAdapter mAdapter;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.long_click_user, menu);
        mFragment.setPullToRefreshEnabled(false);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        WeiboUser user = mAdapter.getSelectedItem();
        mode.setTitle(user.name);
        if (user.following) {
            menu.findItem(R.id.follow).setVisible(false);
            menu.findItem(R.id.unfollow).setVisible(true);
        } else {
            menu.findItem(R.id.follow).setVisible(true);
            menu.findItem(R.id.unfollow).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.follow:
                new FollowTask(mAdapter.getSelectedItem(), new OnFollowFinishedListener(mAdapter.getSelection())).execute();
                break;
            case R.id.unfollow:
                new UnfollowTask(mAdapter.getSelectedItem(), new OnUnfollowFinishedListener(mAdapter.getSelection()))
                        .execute();
                break;
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFragment.onActionModeFinished();
        mAdapter.setSelectedPosition(-1);
        mAdapter.notifyDataSetChanged();
        mFragment.setPullToRefreshEnabled(true);
    }

    public void setFragment(AbsUserListFragment<FriendsUserListAdapter, WeiboUser> fragment) {
        mFragment = fragment;
    }

    public void setAdapter(FriendsUserListAdapter adapter) {
        mAdapter = adapter;
    }

    private class OnFollowFinishedListener implements FollowTask.OnFollowFinishedListener {
        private int mSelection;

        public OnFollowFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onFollowFinished(WeiboUser user) {
            mAdapter.updatePosition(mSelection, user);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFollowFailed(WeiboException e) {
            Utilities.notice(R.string.follow_failed_reason, e.getMessage());
        }
    }

    private class OnUnfollowFinishedListener implements UnfollowTask.OnUnfollowFinishedListener {
        private int mSelection;

        public OnUnfollowFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onUnfollowFinished(WeiboUser user) {
            mAdapter.updatePosition(mSelection, user);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUnfollowFailed(WeiboException e) {
            Utilities.notice(R.string.unfollow_failed_reason, e.getMessage());
        }
    }
}
