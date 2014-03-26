package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.json.JSONObject;

import gov.moandor.androidweibo.activity.UserActivity;
import gov.moandor.androidweibo.adapter.FriendsUserListAdapter;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.FriendsUserListActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

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
        mActionModeCallback = new FriendsUserListActionModeCallback(mAdapter, this);
    }
    
    @Override
    HttpParams getParams() {
        HttpParams params = new HttpParams();
        params.putParam("uid", String.valueOf(mUserId));
        params.putParam("trim_status", "1");
        return params;
    }
    
    @Override
    void onItemClick(int position) {
        WeiboUser user = mAdapter.getItem(position);
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), UserActivity.class);
        intent.putExtra(UserActivity.USER, user);
        getActivity().startActivity(intent);
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
    void onListItemChecked(int position) {
        mAdapter.setSelectedPosition(position);
    }
    
    @Override
    List<WeiboUser> getDataFromJson(JSONObject json) throws WeiboException {
        return Utilities.getWeiboUsersFromJson(json);
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
