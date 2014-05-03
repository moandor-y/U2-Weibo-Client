package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;

import gov.moandor.androidweibo.activity.DmActivity;
import gov.moandor.androidweibo.adapter.DmUserListAdapter;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseUserListDao;
import gov.moandor.androidweibo.dao.DmUserListDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.DmUserListActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;

import java.util.List;

public class DmUserListFragment extends AbsUserListFragment<DmUserListAdapter, DirectMessagesUser> {
    private static final int REQUEST_CODE = 0;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdapter == null) {
            mAdapter = new DmUserListAdapter(this);
        }
        mListView.setAdapter(mAdapter);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        final long accountId = GlobalContext.getCurrentAccount().user.id;
        DirectMessagesUser[] users = mAdapter.getItems();
        final DatabaseUtils.DmUsers dmUsers = new DatabaseUtils.DmUsers();
        dmUsers.users = users;
        dmUsers.nextCursor = mNextCursor;
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseUtils.updateDmUsers(dmUsers, accountId);
            }
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            WeiboUser user = data.getParcelableExtra(DmConversationFragment.RESULT_USER);
            DirectMessage message = data.getParcelableExtra(DmConversationFragment.RESULT_LATEST_MESSAGE);
            int position = mAdapter.findPositionByUserId(user.id);
            DirectMessagesUser item = mAdapter.getItem(position);
            item.message = message;
            mAdapter.updateItem(position, item);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    void initContent() {
        final long accountId = GlobalContext.getCurrentAccount().user.id;
        mRefreshTask = new MyAsyncTask<Void, Void, DatabaseUtils.DmUsers>() {
            @Override
            protected DatabaseUtils.DmUsers doInBackground(Void... params) {
                return DatabaseUtils.getDmUsers(accountId);
            }
            
            @Override
            protected void onPostExecute(DatabaseUtils.DmUsers result) {
                mRefreshTask = null;
                if (result != null) {
                    mAdapter.updateDataSet(result.users);
                    mAdapter.notifyDataSetChanged();
                    mNextCursor = result.nextCursor;
                    if (mNextCursor == 0) {
                        mNoMoreUser = true;
                        hideLoadingFooter();
                    }
                } else {
                    refresh();
                }
            }
        }.execute();
    }
    
    @Override
    protected BaseUserListDao<DirectMessagesUser> onCreateDao() {
        return new DmUserListDao();
    }
    
    @Override
    void onItemClick(int position) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), DmActivity.ConversationActivity.class);
        intent.putExtra(DmActivity.ConversationActivity.USER, mAdapter.getItem(position).weiboUser);
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    @Override
    MyAsyncTask<Void, ?, ?> onCreateRefreshTask() {
        return new DmUserListRefreshTask();
    }
    
    @Override
    MyAsyncTask<Void, ?, ?> onCreateloLoadMoreTask() {
        return new DmUserListLoadMoreTask();
    }
    
    @Override
    protected ActionMode.Callback getActionModeCallback() {
        DmUserListActionModeCallback callback = new DmUserListActionModeCallback();
        callback.setFragment(this);
        callback.setAdapter(mAdapter);
        return callback;
    }
    
    private class DmUserListRefreshTask extends RefreshTask {
        @Override
        protected void onPostExecute(List<DirectMessagesUser> result) {
            super.onPostExecute(result);
            if (result != null) {
                mAdapter.updateDataSet(result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private class DmUserListLoadMoreTask extends LoadMoreTask {
        @Override
        protected void onPostExecute(List<DirectMessagesUser> result) {
            super.onPostExecute(result);
            if (result != null) {
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
