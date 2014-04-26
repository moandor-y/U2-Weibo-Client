package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.view.View;

import gov.moandor.androidweibo.adapter.DmUserListAdapter;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseUserListDao;
import gov.moandor.androidweibo.dao.DirectMessagesUserListDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.DmUserListActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;

import java.util.List;

public class DmUserListFragment extends
        AbsUserListFragment<DmUserListAdapter, DirectMessagesUser> {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdapter == null) {
            mAdapter = new DmUserListAdapter(this);
        }
        mListView.setAdapter(mAdapter);
        mActionModeCallback = new DmUserListActionModeCallback();
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
        return new DirectMessagesUserListDao();
    }
    
    @Override
    void onItemClick(int position) {
        // TODO Auto-generated method stub
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
    void onListItemChecked(int position) {
        // TODO Auto-generated method stub
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
