package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.view.View;

import org.json.JSONObject;

import java.util.List;

import gov.moandor.androidweibo.adapter.DirectMessagesUserListAdapter;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.DmUserListActionModeCallback;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

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
        return Utilities.getDmUsersFromJson(json);
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
