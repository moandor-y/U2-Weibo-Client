package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v7.view.ActionMode.Callback;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.Arrays;
import java.util.List;

import gov.moandor.androidweibo.adapter.DmConversationAdapter;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.DmConversationDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;

public class DmConversationFragment extends AbsTimelineFragment<DirectMessage, DmConversationAdapter> {
    public static final String USER = "user";
    
    private WeiboUser mUser;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        stopRefreshTaskIfRunning();
        mRefreshTask = new LoadFromDatabaseTask();
        mRefreshTask.execute();
    }
    
    @Override
    DmConversationAdapter createListAdapter() {
        return new DmConversationAdapter();
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return null;
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new RefreshTask();
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
    }
    
    @Override
    Callback getActionModeCallback() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected BaseTimelineJsonDao<DirectMessage> onCreateDao() {
        DmConversationDao dao = new DmConversationDao();
        dao.setUid(mUser.id);
        return dao;
    }
    
    @Override
    protected void loadMore() {
        // do nothing
    }
    
    private void stopRefreshTaskIfRunning() {
        if (mRefreshTask != null && mRefreshTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            mRefreshTask.cancel(true);
        }
    }
    
    private class LoadFromDatabaseTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private long mAccountId;
        private long mUserId;
        
        @Override
        protected void onPreExecute() {
            mAccountId = GlobalContext.getCurrentAccount().user.id;
            mUserId = mUser.id;
        }
        
        @Override
        protected List<DirectMessage> doInBackground(Void... params) {
            DirectMessage[] result = DatabaseUtils.getDmConversation(mAccountId, mUserId);
            if (result != null) {
                return Arrays.asList(result);
            } else {
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            if (result != null) {
                mAdapter.updateDataSet(result);
            } else {
                refresh();
            }
            // TODO Auto-generated method stub
        }
    }
}
