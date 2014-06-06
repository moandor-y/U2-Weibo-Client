package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.ISelectableAdapter;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseUserListDao;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class AbsUserListFragment<Adapter extends BaseAdapter, DataBean> extends Fragment {
    public static final String USER_ID = "user_id";
    
    Adapter mAdapter;
    ListView mListView;
    MyAsyncTask<Void, ?, ?> mRefreshTask;
    int mNextCursor;
    boolean mNoMoreUser;
    private int mListScrollState;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFooter;
    private View mFooterIcon;
    private Animation mFooterAnimation = AnimationUtils.loadAnimation(GlobalContext.getInstance(), R.anim.refresh);
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter.getCount() == 0) {
            initContent();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline_list, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setFastScrollEnabled(ConfigManager.isFastScrollEnabled());
        mListView.setOnScrollListener(new OnListScrollListener());
        mListView.setOnItemClickListener(new OnListItemClickListener());
        mListView.setOnItemLongClickListener(new OnListItemLongClickListener());
        mFooter =
                GlobalContext.getActivity().getLayoutInflater()
                        .inflate(R.layout.timeline_list_footer, mListView, false);
        mFooterIcon = mFooter.findViewById(R.id.image);
        showLoadingFooter();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh_color1, R.color.swipe_refresh_color2,
                R.color.swipe_refresh_color3, R.color.swipe_refresh_color4);
        mSwipeRefreshLayout.setOnRefreshListener(new OnListRefreshListener());
    }
    
    public boolean isListViewFling() {
        return mListScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
    }
    
    private boolean isLastItemVisible() {
        return mListView.getFirstVisiblePosition() + mListView.getChildCount() >= mAdapter.getCount() - 1;
    }
    
    private void showLoadingFooter() {
        if (mListView.getFooterViewsCount() == 0) {
            mListView.addFooterView(mFooter);
            mFooterIcon.startAnimation(mFooterAnimation);
        }
    }
    
    void hideLoadingFooter() {
        mListView.removeFooterView(mFooter);
    }
    
    private void loadMore() {
        if (mRefreshTask != null || !mSwipeRefreshLayout.isEnabled() || mNoMoreUser) {
            return;
        }
        mRefreshTask = onCreateloLoadMoreTask();
        mRefreshTask.execute();
    }
    
    void initContent() {
        refresh();
    }
    
    public void refresh() {
        if (mRefreshTask != null || !mSwipeRefreshLayout.isEnabled()) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        mRefreshTask = onCreateRefreshTask();
        mRefreshTask.execute();
    }
    
    public void setPullToRefreshEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }
    
    public void onActionModeFinished() {
        mActionMode = null;
    }
    
    protected void onDaoCreated(BaseUserListDao<DataBean> dao) {}
    
    private class OnListScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mListScrollState = scrollState;
            switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                mAdapter.notifyDataSetChanged();
                ImageDownloader.setPauseImageReadTask(false);
                if (isLastItemVisible()) {
                    loadMore();
                }
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                ImageDownloader.setPauseImageReadTask(true);
                break;
            }
        }
    }
    
    private class OnListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= mAdapter.getCount()) {
                return;
            }
            AbsUserListFragment.this.onItemClick(position);
        }
    }
    
    abstract class RefreshTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        private BaseUserListDao<DataBean> mDao;
        
        @Override
        protected void onPreExecute() {
            mDao = onCreateDao();
            mDao.setTrimStatus(1);
            onDaoCreated(mDao);
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            try {
                List<DataBean> beans = mDao.execute();
                mNextCursor = mDao.getNextCursor();
                return beans;
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DataBean> result) {
            mRefreshTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            hideLoadingFooter();
            if (mNextCursor == 0) {
                mNoMoreUser = true;
            } else {
                mNoMoreUser = false;
            }
        }
    }
    
    abstract class LoadMoreTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        private BaseUserListDao<DataBean> mDao;
        
        @Override
        protected void onPreExecute() {
            showLoadingFooter();
            mDao = onCreateDao();
            mDao.setTrimStatus(1);
            onDaoCreated(mDao);
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
            mDao.setCursor(mNextCursor);
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            try {
                List<DataBean> beans = mDao.execute();
                mNextCursor = mDao.getNextCursor();
                return beans;
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DataBean> result) {
            mRefreshTask = null;
            hideLoadingFooter();
            if (mNextCursor == 0) {
                mNoMoreUser = true;
            }
        }
    }
    
    private class OnListItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActionMode != null) {
                return false;
            }
            ((ISelectableAdapter<?>) mAdapter).setSelectedPosition(position);
            mAdapter.notifyDataSetChanged();
            if (mActionModeCallback == null) {
                mActionModeCallback = getActionModeCallback();
            }
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            return true;
        }
    }
    
    private class OnListRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }
    
    abstract void onItemClick(int position);
    
    abstract MyAsyncTask<Void, ?, ?> onCreateRefreshTask();
    
    abstract MyAsyncTask<Void, ?, ?> onCreateloLoadMoreTask();
    
    protected abstract BaseUserListDao<DataBean> onCreateDao();
    
    protected abstract ActionMode.Callback getActionModeCallback();
}
