package gov.moandor.androidweibo.util;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.adapter.DmUserListAdapter;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.fragment.DmUserListFragment;

public class DmUserListActionModeCallback implements ActionMode.Callback {
    private DmUserListFragment mFragment;
    private DmUserListAdapter mAdapter;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        DirectMessagesUser user = mAdapter.getSelectedItem();
        mode.setTitle(user.weiboUser.name);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFragment.onActionModeFinished();
        mAdapter.setSelectedPosition(-1);
        mAdapter.notifyDataSetChanged();
        mFragment.setPullToRefreshEnabled(true);
    }

    public void setFragment(DmUserListFragment fragment) {
        mFragment = fragment;
    }

    public void setAdapter(DmUserListAdapter adapter) {
        mAdapter = adapter;
    }
}
