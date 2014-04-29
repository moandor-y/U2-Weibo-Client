package gov.moandor.androidweibo.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
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
import android.widget.ListView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.UserActivity;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.adapter.WeiboListAdapter;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class AbsTimelineFragment<DataBean extends AbsItemBean, TimelineListAdapter extends AbsTimelineListAdapter<DataBean>>
        extends Fragment {
    private static final String USER_DIALOG = "user_dialog";
    
    TimelineListAdapter mAdapter;
    ListView mListView;
    ActionMode mActionMode;
    MyAsyncTask<Void, Void, List<DataBean>> mRefreshTask;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private int mListScrollState;
    private boolean mNoEarlierMessage = false;
    private View mFooter;
    private View mFooterIcon;
    private TextView mFooterText;
    private ActionMode.Callback mActionModeCallback;
    private Animation mFooterAnimation = AnimationUtils.loadAnimation(GlobalContext.getInstance(), R.anim.refresh);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline_list, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupListHwAccel();
        }
        mListView.setFastScrollEnabled(ConfigManager.isFastScrollEnabled());
        mListView.setOnScrollListener(new OnListScrollListener());
        mListView.setOnItemLongClickListener(new OnListItemLongClickListener());
        buildLoadingFooter();
        showLoadingFooter();
        if (mAdapter == null) {
            mAdapter = createListAdapter();
        }
        mAdapter.setFragment(this);
        mAdapter.setOnAvatarClickListener(new OnAvatarClickListener());
        mAdapter.setOnAvatarLongClickListener(new OnAvatarLongClickListener());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnListItemClickListener());
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(getOnRefreshListener());
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh_color1, R.color.swipe_refresh_color2,
                R.color.swipe_refresh_color3, R.color.swipe_refresh_color4);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mAdapter.updateState();
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }
	
	protected SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
		return new OnListRefreshListener();
	}
	
	private void buildLoadingFooter() {
		mFooter =
			GlobalContext.getActivity().getLayoutInflater()
			.inflate(R.layout.timeline_list_footer, mListView, false);
        mFooterIcon = mFooter.findViewById(R.id.image);
        mFooterText = (TextView) mFooter.findViewById(R.id.text);
	}
    
	private void showLoadingFooter() {
		if (mFooter == null) {
			return;
		}
        if (mListView.getFooterViewsCount() == 0) {
            mListView.addFooterView(mFooter);
            mFooterIcon.startAnimation(mFooterAnimation);
        }
    }
	
    private void hideLoadingFooter() {
		if (mFooter == null) {
			return;
		}
        mListView.removeFooterView(mFooter);
    }
	
    public boolean isListViewFling() {
        return mListScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
    }
    
    public boolean hasActionMode() {
        return mActionMode != null;
    }
    
    public void setPullToRefreshEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }
	
    protected void loadMore() {
        if ((mRefreshTask != null && mRefreshTask.getStatus() != MyAsyncTask.Status.FINISHED)
                || !mSwipeRefreshLayout.isEnabled() || mNoEarlierMessage) {
            return;
        }
        mRefreshTask = createLoadMoreTask();
        mRefreshTask.execute();
        mAdapter.updateState();
        mAdapter.notifyDataSetChanged();
    }
    
    public void refresh() {
        if ((mRefreshTask != null && mRefreshTask.getStatus() != MyAsyncTask.Status.FINISHED)
                || !mSwipeRefreshLayout.isEnabled()) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        mRefreshTask = createRefreshTask();
        mRefreshTask.execute();
        mAdapter.updateState();
        mAdapter.notifyDataSetChanged();
    }
    
    private boolean isLastItemVisible() {
        return mListView.getFirstVisiblePosition() + mListView.getChildCount() >= mAdapter.getCount() - 1;
    }
    
    public void onActionModeFinished() {
        mActionMode = null;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupListHwAccel() {
        if (!ConfigManager.isListHwAccelEnabled()) {
            mListView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    
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
            AbsTimelineFragment.this.onItemClick(parent, view, position, id);
        }
    }
    
    private class OnAvatarClickListener implements WeiboListAdapter.OnAvatarClickListener {
        @Override
        public void onAvatarClick(int position) {
            WeiboUser user = mAdapter.getItem(position).weiboUser;
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), UserActivity.class);
            intent.putExtra(UserActivity.USER, user);
            getActivity().startActivity(intent);
        }
    };
    
    private class OnAvatarLongClickListener implements WeiboListAdapter.OnAvatarLongClickListener {
        @Override
        public void onAvatarLongClick(int position) {
            UserDialogFragment dialog = new UserDialogFragment();
            Bundle args = new Bundle();
            WeiboUser user = mAdapter.getItem(position).weiboUser;
            args.putParcelable(UserDialogFragment.USER, user);
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), USER_DIALOG);
        }
    };
    
    private class OnListRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }
    
    class RefreshTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        private BaseTimelineJsonDao<DataBean> mDao;
		
        @Override
        protected void onPreExecute() {
			DataBean latestMessage = null;
            if (mAdapter.getCount() > 0) {
                latestMessage = mAdapter.getItem(0);
            }
			mDao = onCreateDao();
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
            mDao.setSinceMessage(latestMessage);
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            try {
                return mDao.execute();
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
            mNoEarlierMessage = false;
            mFooterText.setText(R.string.loading);
            mFooterIcon.setVisibility(View.VISIBLE);
            if (result != null && result.size() >= 1) {
                if (mDao.noEnoughNewMessages()) {
                    mAdapter.addAllFirst(result);
                } else {
                    mAdapter.updateDataSet(result);
                }
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(0);
            }
        }
    }
    
    class LoadMoreTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        private BaseTimelineJsonDao<DataBean> mDao;
		
        @Override
        protected void onPreExecute() {
			long maxId = 0L;
            if (mAdapter.getCount() >= 1) {
                maxId = mAdapter.getItemId(mAdapter.getCount() - 1) - 1;
            }
            showLoadingFooter();
			mDao = onCreateDao();
            mDao.setToken(GlobalContext.getCurrentAccount().token);
            mDao.setCount(Utilities.getLoadWeiboCount());
            mDao.setMaxId(maxId);
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            try {
                return mDao.execute();
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
            if (result != null && result.size() == 0) {
                mNoEarlierMessage = true;
                mFooterText.setText(R.string.no_earlier_message);
                mFooterIcon.clearAnimation();
                mFooterIcon.setVisibility(View.GONE);
            } else {
                hideLoadingFooter();
            }
            if (result != null) {
                mAdapter.addAll(result);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private class OnListItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActionMode != null || position < 0 || mAdapter.getCount() <= position) {
                return false;
            }
            mAdapter.setSelectedPosition(position);
            mAdapter.notifyDataSetChanged();
            if (mActionModeCallback == null) {
                mActionModeCallback = getActionModeCallback();
            }
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            return true;
        }
    }
    
    abstract TimelineListAdapter createListAdapter();
    
    abstract LoadMoreTask createLoadMoreTask();
    
    abstract RefreshTask createRefreshTask();
    
    abstract void onItemClick(AdapterView<?> parent, View view, int position, long id);
    
    abstract ActionMode.Callback getActionModeCallback();
    
    protected abstract BaseTimelineJsonDao<DataBean> onCreateDao();
}
