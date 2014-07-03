package gov.moandor.androidweibo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;

import java.util.Arrays;
import java.util.List;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.DmConversationAdapter;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.DmConversationDao;
import gov.moandor.androidweibo.notification.SendDmService;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.DmConversationActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class DmConversationFragment extends AbsTimelineFragment<DirectMessage, DmConversationAdapter> {
    public static final int SEND_SUCCESSFUL = 0;
    public static final int SEND_FAILED = 1;
    public static final String USER = "user";
    public static final String SEND_FINISHED = Utilities.buildIntentExtraName("SEND_FINISHED");
    public static final String SEND_RESULT_CODE = Utilities.buildIntentExtraName("SEND_RESULT_CODE");
    public static final String SEND_SUCCESSFUL_MESSAGE = Utilities.buildIntentExtraName("SEND_SUCCESSFUL_MESSAGE");
    public static final String SEND_FAILED_TEXT = Utilities.buildIntentExtraName("SEND_FAILED_TEXT");
    public static final String SEND_FAILED_ERROR = Utilities.buildIntentExtraName("SEND_FAILED_ERROR");
    private BroadcastReceiver mSendFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(SEND_RESULT_CODE, -1);
            switch (resultCode) {
                case SEND_SUCCESSFUL:
                    DirectMessage message = intent.getParcelableExtra(SEND_SUCCESSFUL_MESSAGE);
                    mAdapter.addFirst(message);
                    mAdapter.notifyDataSetChanged();
                    updateDatabase();
                    break;
                case SEND_FAILED:
                    String error = intent.getStringExtra(SEND_FAILED_ERROR);
                    String text = intent.getStringExtra(SEND_FAILED_TEXT);
                    mEditText.setError(error);
                    mEditText.setText(text);
                    break;
            }
        }
    };
    public static final String RESULT_USER = Utilities.buildIntentExtraName("RESULT_USER");
    public static final String RESULT_LATEST_MESSAGE = Utilities.buildIntentExtraName("RESULT_LATEST_MESSAGE");
    private static final long LOAD_INTERVAL = 3 * 60 * 1000;
    private static final int MAX_DATABASE_MESSAGE_COUNT = 100;
    private static final int RESULT_CODE = 0;
    private boolean mRunning;
    private WeiboUser mUser;
    private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEND_FINISHED);
        Utilities.registerReceiver(mSendFinishReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dm_conversation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText = (EditText) view.findViewById(R.id.send_dm);
        view.findViewById(R.id.button_send).setOnClickListener(new OnSendButtonClickListener());
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
    protected SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new OnListRefreshListener();
    }

    @Override
    protected void loadMore() {/* do nothing */}

    @Override
    public void refresh() {/* do nothing */}

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
    public void onDestroy() {
        super.onDestroy();
        Utilities.unregisterReceiver(mSendFinishReceiver);
    }

    private void startAutoLoadNewMessages() {
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRefreshTaskRunning()) {
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

    private void updateDatabase() {
        final long accountId = GlobalContext.getCurrentAccount().user.id;
        final long userId = mUser.id;
        final List<DirectMessage> messages = mAdapter.getItems();
        if (messages.size() > MAX_DATABASE_MESSAGE_COUNT) {
            messages.subList(MAX_DATABASE_MESSAGE_COUNT, messages.size()).clear();
        }
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseUtils.updateDmConversation(accountId, userId, messages.toArray(new DirectMessage[0]));
            }
        });
        Intent data = new Intent();
        data.putExtra(RESULT_USER, mUser);
        data.putExtra(RESULT_LATEST_MESSAGE, mAdapter.getItem(0));
        getActivity().setResult(RESULT_CODE, data);
    }

    private class LoadFromDatabaseTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private long mAccountId;
        private long mUserId;

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
        protected void onPreExecute() {
            mAccountId = GlobalContext.getCurrentAccount().user.id;
            mUserId = mUser.id;
        }


        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            if (result != null) {
                mAdapter.updateDataSet(result);
                mAdapter.notifyDataSetChanged();
                hideLoadingFooter();
                mRefreshTask = new LoadNewMessagesTask();
                mRefreshTask.execute();
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
                Logger.logException(e);
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
                Logger.logException(e);
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
                updateDatabase();
            }
        }
    }

    private class LoadNewMessagesTask extends MyAsyncTask<Void, Void, List<DirectMessage>> {
        private BaseTimelineJsonDao<DirectMessage> mDao;
        private List<DirectMessage> mLastResult;

        private LoadNewMessagesTask() {
        }

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
            if (mLastResult != null && mLastResult.size() > 0) {
                mDao.setMaxId(mLastResult.get(mLastResult.size() - 1).id - 1);
            }
        }

        @Override
        protected List<DirectMessage> doInBackground(Void... v) {
            try {
                return mDao.execute();
            } catch (WeiboException e) {
                Logger.logException(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<DirectMessage> result) {
            mRefreshTask = null;
            if (result != null) {
                if (mDao.noEnoughNewMessages()) {
                    if (mLastResult != null) {
                        result.addAll(0, mLastResult);
                    }
                    mAdapter.addAllFirst(result);
                    mAdapter.notifyDataSetChanged();
                    updateDatabase();
                } else {
                    if (mLastResult == null) {
                        mRefreshTask = new LoadNewMessagesTask(result);
                    } else {
                        mLastResult.addAll(result);
                        mRefreshTask = new LoadNewMessagesTask(mLastResult);
                    }
                    mRefreshTask.execute();
                }
            }
        }
    }

    private class OnListRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            if (isRefreshTaskRunning() || !mSwipeRefreshLayout.isEnabled()) {
                return;
            }
            mRefreshTask = new LoadEarlierMessagesTask();
            mRefreshTask.execute();
            mAdapter.updateState();
            mAdapter.notifyDataSetChanged();
        }
    }

    private class OnSendButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!TextUtils.isEmpty(mEditText.getText())) {
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), SendDmService.class);
                intent.putExtra(SendDmService.TOKEN, GlobalContext.getCurrentAccount().token);
                intent.putExtra(SendDmService.TEXT, mEditText.getText().toString());
                intent.putExtra(SendDmService.USER_ID, mUser.id);
                getActivity().startService(intent);
                mEditText.setText(null);
            }
        }
    }
}
