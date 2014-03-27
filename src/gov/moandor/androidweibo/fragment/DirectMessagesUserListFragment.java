package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.view.View;

import org.json.JSONObject;

import gov.moandor.androidweibo.adapter.DirectMessagesUserListAdapter;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.DmUserListActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class DirectMessagesUserListFragment extends
        AbsUserListFragment<DirectMessagesUserListAdapter, DirectMessagesUser> {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAdapter == null) {
            mAdapter = new DirectMessagesUserListAdapter(this);
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
    String getUrl() {
        return HttpUtils.UrlHelper.DIRECT_MESSAGES_USER_LIST;
    }
    
    @Override
    HttpParams getParams() {
        HttpParams params = new HttpParams();
        return params;
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
    
    @Override
    List<DirectMessagesUser> getDataFromJson(JSONObject json) throws WeiboException {
        return JsonUtils.getDmUsersFromJson(json);
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
