package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v7.view.ActionMode.Callback;
import android.view.View;

import gov.moandor.androidweibo.adapter.FriendsUserListAdapter;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.dao.BaseUserListDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.FriendsUserListActionModeCallback;

import java.util.List;

public abstract class AbsFriendsUserListFragment extends AbsUserListFragment<FriendsUserListAdapter, WeiboUser> {
    private long mUserId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getLong(USER_ID);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdapter == null) {
            mAdapter = new FriendsUserListAdapter();
        }
        mAdapter.setFragment(this);
        mListView.setAdapter(mAdapter);
    }
    
    @Override
    protected void onDaoCreated(BaseUserListDao<WeiboUser> dao) {
        dao.setUid(mUserId);
    }
    
    @Override
    void onItemClick(int position) {
        WeiboUser user = mAdapter.getItem(position);
        getActivity().startActivity(ActivityUtils.userActivity(user));
    }
    
    @Override
    RefreshTask onCreateRefreshTask() {
        return new FriendsUserListRefreshTask();
    }
    
    @Override
    LoadMoreTask onCreateloLoadMoreTask() {
        return new FriendsUserListLoadMoreTask();
    }
    
    @Override
    protected Callback getActionModeCallback() {
        FriendsUserListActionModeCallback callback = new FriendsUserListActionModeCallback();
        callback.setFragment(this);
        callback.setAdapter(mAdapter);
        return callback;
    }
    
    private class FriendsUserListRefreshTask extends RefreshTask {
        @Override
        protected void onPostExecute(List<WeiboUser> result) {
            super.onPostExecute(result);
            if (result != null) {
                mAdapter.updateDataSet(result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private class FriendsUserListLoadMoreTask extends LoadMoreTask {
        @Override
        protected void onPostExecute(List<WeiboUser> result) {
            super.onPostExecute(result);
            if (result != null) {
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
