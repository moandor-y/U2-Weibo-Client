package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.activity.ImageViewerActivity;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboListAdapter;
import gov.moandor.androidweibo.bean.TimelinePosition;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.concurrency.WifiAutoDownloadPicRunnable;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.BilateralTimelineDao;
import gov.moandor.androidweibo.dao.FriendsTimelineDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

import java.util.List;

public class WeiboListFragment extends AbsMainTimelineFragment<WeiboStatus, WeiboListAdapter> {
    private static final int GROUP_ALL = 0;
    private static final int GROUP_BILATERAL = 1;
    private static final int REQUEST_CODE = 0;
    private static final int WIFI_AUTO_DOWNLOAD_PRIORITY = 2;
    
    private Thread mWifiAutoDownloadThread;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setOnPictureClickListener(new OnPictureClickListener());
        mAdapter.setOnMultiPictureClickListener(new OnMultiPictureClickListener());
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            final int position = data.getIntExtra(WeiboActivity.POSITION, -1);
            final WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            mAdapter.updatePosition(position, status);
            mAdapter.notifyDataSetChanged();
            final long accountId = GlobalContext.getCurrentAccount().user.id;
            final int group = GlobalContext.getWeiboGroup();
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils.updateWeiboStatus(status, position, accountId, group);
                }
            });
        }
    }
    
    private void startWifiAutoDownloadPic(int position) {
        if (!GlobalContext.isWifiAutoDownloadPicEnabled() || !GlobalContext.isInWifi()) {
            return;
        }
        if (mWifiAutoDownloadThread != null && mWifiAutoDownloadThread.getState() != Thread.State.TERMINATED) {
            mWifiAutoDownloadThread.interrupt();
        }
        mWifiAutoDownloadThread = new Thread(new WifiAutoDownloadPicRunnable(mAdapter.getItems(), position));
        mWifiAutoDownloadThread.setPriority(WIFI_AUTO_DOWNLOAD_PRIORITY);
        mWifiAutoDownloadThread.start();
    }
    
    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }
    
    @Override
    List<WeiboStatus> getBeansFromJson(String json) throws WeiboException {
        return Utilities.getWeiboStatusesFromJson(json);
    }
    
    @Override
    List<WeiboStatus> getBeansFromDatabase() {
        return DatabaseUtils.getWeiboStatuses(GlobalContext.getCurrentAccount().user.id, GlobalContext.getWeiboGroup());
    }
    
    @Override
    void saveRefreshResultToDatabase(List<WeiboStatus> statuses) {
        final long accountId = GlobalContext.getCurrentAccount().user.id;
        final int group = GlobalContext.getWeiboGroup();
        DatabaseUtils.removeWeiboStatuses(accountId, group);
        DatabaseUtils.insertWeiboStatuses(statuses, accountId, group);
    }
    
    @Override
    void saveLoadMoreResultToDatabase(SparseArray<WeiboStatus> statuses) {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int group = GlobalContext.getWeiboGroup();
        DatabaseUtils.insertWeiboStatuses(statuses, accountId, group);
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        switch (GlobalContext.getWeiboGroup()) {
        case GROUP_BILATERAL:
            return new BilateralTimelineDao();
        case GROUP_ALL:
        default:
            return new FriendsTimelineDao();
        }
    }
    
    @Override
    public void saveListPosition() {
        View view = mListView.getChildAt(0);
        if (view != null) {
            final TimelinePosition position = new TimelinePosition();
            position.position = mListView.getFirstVisiblePosition();
            position.top = view.getTop();
            final long accountId = GlobalContext.getCurrentAccount().user.id;
            final int group = GlobalContext.getWeiboGroup();
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils.insertOrUpdateTimelinePosition(position, MainActivity.WEIBO_LIST, group, accountId);
                }
            });
            
        }
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WeiboActivity.class);
        intent.putExtra(WeiboActivity.POSITION, position);
        intent.putExtra(WeiboActivity.WEIBO_STATUS, mAdapter.getItem(position));
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        WeiboListActionModeCallback callback = new WeiboListActionModeCallback() {
            @Override
            protected void removeFromDatabase(final int position, final long accountId, final int group) {
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseUtils.removeWeiboStatus(position, accountId, group);
                    }
                });
            }
            
            @Override
            protected void updateDatabase(final WeiboStatus status, final int position, final long accountId,
                    final int group) {
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseUtils.updateWeiboStatus(status, position, accountId, group);
                    }
                });
            }
        };
        callback.setAdapter(mAdapter);
        callback.setFragment(this);
        return callback;
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new WeiboListRefreshTask();
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new WeiboListLoadMoreTask();
    }
    
    @Override
    TimelinePosition onRestoreListPosition() {
        return DatabaseUtils.getTimelinePosition(MainActivity.WEIBO_LIST, GlobalContext.getWeiboGroup());
    }
    
    @Override
    public void notifyAccountOrGroupChanged() {
        super.notifyAccountOrGroupChanged();
        ((MainActivity) getActivity()).resetWeiboUnreadCount();
    }
    
    private class OnMultiPictureClickListener implements WeiboListAdapter.OnMultiPictureClickListener {
        @Override
        public void onMultiPictureClick(int position, int picIndex) {
            WeiboStatus status = mAdapter.getItem(position);
            if (status.retweetStatus != null) {
                status = status.retweetStatus;
            }
            ImageViewerActivity.start(status, picIndex, getActivity());
        }
    }
    
    private class OnPictureClickListener implements WeiboListAdapter.OnPictureClickListener {
        @Override
        public void onPictureClick(int position) {
            WeiboStatus status = mAdapter.getItem(position);
            if (status.retweetStatus != null) {
                status = status.retweetStatus;
            }
            ImageViewerActivity.start(status, 0, getActivity());
        }
    }
    
    private class WeiboListRefreshTask extends MainRefreshTask {
        @Override
        protected void onPostExecute(List<WeiboStatus> result) {
            super.onPostExecute(result);
            if (result != null && result.size() > 0) {
                startWifiAutoDownloadPic(0);
                ((MainActivity) getActivity()).resetWeiboUnreadCount();
            }
        }
    }
    
    private class WeiboListLoadMoreTask extends MainLoadMoreTask {
        @Override
        protected void onPostExecute(List<WeiboStatus> result) {
            super.onPostExecute(result);
            if (result != null && result.size() > 0) {
                startWifiAutoDownloadPic(mListView.getFirstVisiblePosition());
            }
        }
    }
}
