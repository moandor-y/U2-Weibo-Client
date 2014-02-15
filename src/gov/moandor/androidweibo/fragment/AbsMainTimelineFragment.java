package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.bean.TimelinePosition;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.GlobalContext;

import java.util.List;

public abstract class AbsMainTimelineFragment<DataBean extends AbsItemBean, TimelineListAdapter extends AbsTimelineListAdapter<DataBean>>
        extends AbsTimelineFragment<DataBean, TimelineListAdapter> {
    public static final String IS_FROM_UNREAD = "is_from_unread";
    
    private boolean mIsFromUnread = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mIsFromUnread = args != null && args.getBoolean(IS_FROM_UNREAD);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter.getCount() == 0 || mIsFromUnread) {
            mRefreshTask = new LoadFromDatabaseTask();
            mRefreshTask.execute();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        saveListPosition();
    }
    
    @Override
    boolean isThisCurrentFragment() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            return activity.isCurrentFragment(this);
        }
        return false;
    }
    
    public void notifyAccountOrGroupChanged() {
        if (mIsFromUnread) {
            return;
        }
        if (mRefreshTask != null) {
            mRefreshTask.cancel(true);
        }
        mPullToRefreshAttacher.setRefreshComplete();
        if (mActionMode != null) {
            mActionMode.finish();
        }
        mRefreshTask = new LoadFromDatabaseTask();
        mRefreshTask.execute();
    }
    
    private void restoreListPosition() {
        mListView.setVisibility(View.INVISIBLE);
        new MyAsyncTask<Void, Void, TimelinePosition>() {
            @Override
            protected TimelinePosition doInBackground(Void... params) {
                return onRestoreListPosition();
            }
            
            @Override
            protected void onPostExecute(TimelinePosition result) {
                onListPositionRestored(result);
            }
        }.execute();
    }
    
    void onListPositionRestored(TimelinePosition result) {
        mListView.setSelectionFromTop(result.position, result.top);
        mListView.setVisibility(View.VISIBLE);
    }
    
    class MainRefreshTask extends RefreshTask {
        @Override
        protected void onPostExecute(List<DataBean> result) {
            super.onPostExecute(result);
            if (result != null) {
                int updatedCount = result.size();
                String toast;
                if (updatedCount > 0) {
                    toast = GlobalContext.getInstance().getString(R.string.new_posts_updated, updatedCount);
                    final List<DataBean> beans = mAdapter.getItems();
                    MyAsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            saveRefreshResultToDatabase(beans);
                        }
                    });
                } else {
                    toast = GlobalContext.getInstance().getString(R.string.no_new_posts);
                }
                if (isThisCurrentFragment()) {
                    Toast.makeText(GlobalContext.getInstance(), toast, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    class MainLoadMoreTask extends LoadMoreTask {
        @Override
        protected void onPostExecute(List<DataBean> result) {
            super.onPostExecute(result);
            if (result != null) {
                final SparseArray<DataBean> beans = new SparseArray<DataBean>();
                for (DataBean bean : result) {
                    beans.append(mAdapter.positionOf(bean), bean);
                }
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        saveLoadMoreResultToDatabase(beans);
                    }
                });
            }
        }
    }
    
    private class LoadFromDatabaseTask extends MyAsyncTask<Void, Void, List<DataBean>> {
        @Override
        protected void onPreExecute() {
            setPullToRefreshEnabled(false);
            mAdapter.clearDataSet();
            mAdapter.notifyDataSetChanged();
        }
        
        @Override
        protected List<DataBean> doInBackground(Void... v) {
            return getBeansFromDatabase();
        }
        
        @Override
        protected void onPostExecute(List<DataBean> result) {
            mRefreshTask = null;
            setPullToRefreshEnabled(true);
            if (result.size() > 0) {
                mAdapter.updateDataSet(result);
                mAdapter.notifyDataSetChanged();
                restoreListPosition();
                if (mIsFromUnread) {
                    mIsFromUnread = false;
                    refresh();
                }
            } else {
                refresh();
            }
        }
    }
    
    abstract List<DataBean> getBeansFromDatabase();
    
    abstract void saveRefreshResultToDatabase(List<DataBean> beans);
    
    abstract void saveLoadMoreResultToDatabase(SparseArray<DataBean> beans);
    
    public abstract void saveListPosition();
    
    abstract TimelinePosition onRestoreListPosition();
}
