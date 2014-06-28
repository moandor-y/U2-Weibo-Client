package gov.moandor.androidweibo.util;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.adapter.DmConversationAdapter;
import gov.moandor.androidweibo.fragment.DmConversationFragment;

public class DmConversationActionModeCallback implements ActionMode.Callback {
    private DmConversationFragment mFragment;
    private DmConversationAdapter mAdapter;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
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

    public void setFragment(DmConversationFragment fragment) {
        mFragment = fragment;
    }

    public void setAdapter(DmConversationAdapter adapter) {
        mAdapter = adapter;
    }
}
