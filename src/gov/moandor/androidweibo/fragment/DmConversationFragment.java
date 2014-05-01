package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import gov.moandor.androidweibo.adapter.DmConversationAdapter;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.DmConversationDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.DmConversationActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.Arrays;
import java.util.List;

public class DmConversationFragment extends AbsTimelineFragment<DirectMessage, DmConversationAdapter> {
    public static final String USER = "user";
    private static final long LOAD_INTERVAL = 3 * 60 * 1000;
    private static final int MAX_DATABASE_MESSAGE_COUNT = 100;
    
    private boolean mRunning;
    private WeiboUser mUser;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        stopRefreshTaskIfRunning();
        mRefreshTask = new LoadFromDatabaseTask();
        mRefreshTask.execute();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mRunning = true;
        startAutoLoadNewMessages();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mRunning = false;
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
        return null;
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        DmConversationActionModeCallback callback = new DmConversationActionModeCallback();
        callback.setFragment(this);
        callback.setAdapter(mAdapter);
        return callback;
    }
    
    @Override
    protected BaseTimelineJsonDao<DirectMessage> onCreateDao() {
        DmConversationDao dao = new DmConversationDao();
        dao.setUid(mUser.id);
        return dao;
    }
    
    @Override
    public void refresh() {/* do nothing */}
    
    @Override
    protected void loadMore() {/* do nothing */}
    
    @Override
    protected SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new OnListRefreshListener();
    }
    
    private void startAutoLoadNewMessages() {
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRefreshTaskIfRunning()) {
                    return;
                }
                mRefreshTask = new LoadNewMessagesTask();
                mRefreshTask.execute();
                if (mRunning) {
                    GlobalContext.runOnUiThread(this, LOAD_INTERVAL);
                }
            }
        });
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
                mAdapter.notifyDataSetChanged();
                hideLoadingFooter();
            } else {
                mSwipeRefreshLayout.setRefreshing(true);
                mRefreshTask = new FetchMessagesTask();
                mRefreshTask.execute();
                mAdapter.updateState();
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private class LoadEarlierMessagesTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private BaseTimelineJsonDao<DirectMessage> mDao;
        
        @Override
        protected void onPreExecute() {
            long maxId = 0L;
            if (mAdapter.getCount() >= 1) {
                maxId = mAdapter.getItemId(mAdapter.getCount() - 1) - 1;
            }
            mDao = onCreateDao();
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
            mDao.setMaxId(maxId);
        }
        
        @Override
        protected List<DirectMessage> doInBackground(Void... v) {
            try {
                return mDao.execute();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            if (result != null && result.size() > 0) {
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(result.size());
            }
        }
    }
    
    private class FetchMessagesTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private BaseTimelineJsonDao<DirectMessage> mDao;
        
        @Override
        protected void onPreExecute() {
            mDao = onCreateDao();
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
        }
        
        @Override
        protected List<DirectMessage> doInBackground(Void... v) {
            try {
                return mDao.execute();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            hideLoadingFooter();
            if (result != null && result.size() >= 1) {
                mAdapter.updateDataSet(result);
                mAdapter.notifyDataSetChanged();
                final long accountId = GlobalContext.getCurrentAccount().user.id;
                final long userId = mUser.id;
                final DirectMessage[] messages = mAdapter.getItems().toArray(new DirectMessage[0]);
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseUtils.updateDmConversation(accountId, userId, messages);
                    }
                });
            }
        }
    }
    
    private class LoadNewMessagesTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private BaseTimelineJsonDao<DirectMessage> mDao;
        private List<DirectMessage> mLastResult;
        
        private LoadNewMessagesTask() {}
        
        private LoadNewMessagesTask(List<DirectMessage> lastResult) {
            mLastResult = lastResult;
        }
        
        @Override
        protected void onPreExecute() {
            DirectMessage latestMessage = null;
            if (mAdapter.getCount() > 0) {
                latestMessage = mAdapter.getItem(0);
            }
            mDao = onCreateDao();
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
            mDao.setSinceMessage(latestMessage);
            if (mLastResult != null) {
                mDao.setMaxId(mLastResult.get(mLastResult.size() - 1).id - 1);
            }
        }
        
        @Override
        protected List<DirectMessage> doInBackground(Void... v) {
            try {
                return mDao.execute();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            if (result != null) {
                if (mDao.noEnoughNewMessages()) {
                    if (mLastResult == null) {
                        mRefreshTask = new LoadNewMessagesTask(result);
                    } else {
                        mLastResult.addAll(result);
                        mRefreshTask = new LoadNewMessagesTask(mLastResult);
                    }
                    mRefreshTask.execute();
                } else {
                    if (mLastResult != null) {
                        result.addAll(0, mLastResult);
                    }
                    mAdapter.addAllFirst(result);
                    mAdapter.notifyDataSetChanged();
                    final List<DirectMessage> messages = mAdapter.getItems();
                    if (messages.size() > MAX_DATABASE_MESSAGE_COUNT) {
                        messages.subList(MAX_DATABASE_MESSAGE_COUNT, messages.size()).clear();
                    }
                    final long accountId = GlobalContext.getCurrentAccount().user.id;
                    final long userId = mUser.id;
                    MyAsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseUtils.updateDmConversation(accountId, userId, 
                                    messages.toArray(new DirectMessage[0]));
                        }
                    });
                }
            }
        }
    }
    
    private class OnListRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            if (mRefreshTask != null && mRefreshTask.getStatus() != MyAsyncTask.Status.FINISHED
                    || !mSwipeRefreshLayout.isEnabled()) {
                return;
            }
            mRefreshTask = new LoadEarlierMessagesTask();
            mRefreshTask.execute();
            mAdapter.updateState();
            mAdapter.notifyDataSetChanged();
        }
    }
}
