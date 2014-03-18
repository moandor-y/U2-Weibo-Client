package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class AbsUserListFragment<Adapter extends BaseAdapter, DataBean> extends Fragment {
    public static final String USER_ID = "user_id";
    
    Adapter mAdapter;
    ListView mListView;
    ActionMode.Callback mActionModeCallback;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private View mFooter;
    private View mFooterIcon;
    private Animation mFooterAnimation = AnimationUtils.loadAnimation(GlobalContext.getInstance(), R.anim.refresh);
    private MyAsyncTask<Void, ?, ?> mRefreshTask;
    private ActionMode mActionMode;
    private int mListScrollState;
    private int mNextCursor;
    private boolean mNoMoreUser;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPullToRefreshAttacher = ((PullToRefreshAttacherOwner) getActivity()).getAttacher();
        mPullToRefreshAttacher.addRefreshableView(mListView, new OnListRefreshListener());
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
        mListView.setFastScrollEnabled(GlobalContext.isFastScrollEnabled());
        mListView.setOnScrollListener(new OnListScrollListener());
        mListView.setOnItemClickListener(new OnListItemClickListener());
        mListView.setOnItemLongClickListener(new OnListItemLongClickListener());
        mFooter =
                GlobalContext.getActivity().getLayoutInflater()
                        .inflate(R.layout.timeline_list_footer, mListView, false);
        mFooterIcon = mFooter.findViewById(R.id.image);
        showLoadingFooter();
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
    
    private void hideLoadingFooter() {
        mListView.removeFooterView(mFooter);
    }
    
    private void loadMore() {
        if (mRefreshTask != null || !mPullToRefreshAttacher.isEnabled() || mNoMoreUser) {
            return;
        }
        mPullToRefreshAttacher.setRefreshing(true);
        mRefreshTask = onCreateloLoadMoreTask();
        mRefreshTask.execute();
    }
    
    void initContent() {
        refresh();
    }
    
    public void refresh() {
        if (mRefreshTask != null || !mPullToRefreshAttacher.isEnabled()) {
            return;
        }
        mPullToRefreshAttacher.setRefreshing(true);
        mRefreshTask = onCreateRefreshTask();
        mRefreshTask.execute();
    }
    
    public void setPullToRefreshEnabled(boolean enabled) {
        mPullToRefreshAttacher.setEnabled(enabled);
    }
    
    public void onActionModeFinished() {
        mActionMode = null;
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
            AbsUserListFragment.this.onItemClick(position);
        }
    }
    
    abstract class RefreshTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            String url = getUrl();
            HttpParams params = getParams();
            params.addParam("access_token", GlobalContext.getCurrentAccount().token);
            params.addParam("count", String.valueOf(Utilities.getLoadWeiboCount()));
            try {
                String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
                JSONObject json = new JSONObject(response);
                List<DataBean> beans = getDataFromJson(json);
                mNextCursor = json.getInt("next_cursor");
                return beans;
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            } catch (JSONException e) {
                Logger.logExcpetion(e);
                Utilities.notice(R.string.json_error);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DataBean> result) {
            mRefreshTask = null;
            mPullToRefreshAttacher.setRefreshComplete();
            hideLoadingFooter();
            if (mNextCursor == 0) {
                mNoMoreUser = true;
            } else {
                mNoMoreUser = false;
            }
        }
    }
    
    abstract class LoadMoreTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        @Override
        protected void onPreExecute() {
            showLoadingFooter();
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            String url = getUrl();
            HttpParams params = getParams();
            params.addParam("access_token", GlobalContext.getCurrentAccount().token);
            params.addParam("count", String.valueOf(Utilities.getLoadWeiboCount()));
            params.addParam("cursor", String.valueOf(mNextCursor));
            try {
                String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
                JSONObject json = new JSONObject(response);
                List<DataBean> beans = getDataFromJson(json);
                mNextCursor = json.getInt("next_cursor");
                return beans;
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                return null;
            } catch (JSONException e) {
                Logger.logExcpetion(e);
                Utilities.notice(R.string.json_error);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(List<DataBean> result) {
            mRefreshTask = null;
            hideLoadingFooter();
            mPullToRefreshAttacher.setRefreshComplete();
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
            onListItemChecked(position);
            mAdapter.notifyDataSetChanged();
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            return true;
        }
    }
    
    private class OnListRefreshListener implements PullToRefreshAttacher.OnRefreshListener {
        @Override
        public void onRefreshStarted(View view) {
            refresh();
        }
    }
    
    abstract String getUrl();
    
    abstract HttpParams getParams();
    
    abstract void onItemClick(int position);
    
    abstract MyAsyncTask<Void, ?, ?> onCreateRefreshTask();
    
    abstract MyAsyncTask<Void, ?, ?> onCreateloLoadMoreTask();
    
    abstract void onListItemChecked(int position);
    
    abstract List<DataBean> getDataFromJson(JSONObject json) throws WeiboException;
}
