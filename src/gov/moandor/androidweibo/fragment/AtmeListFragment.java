package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboListAdapter;
import gov.moandor.androidweibo.bean.TimelinePosition;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.MentionsWeiboTimelineDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

import java.util.List;

public class AtmeListFragment extends AbsMainTimelineFragment<WeiboStatus, WeiboListAdapter> {
    private static final int REQUEST_CODE = 0;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setOnPictureClickListener(new OnPictureClickListener());
        mAdapter.setOnMultiPictureClickListener(new OnMultiPictureClickListener());
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            final WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            final int position = mAdapter.positionOf(status.id);
			if (0 <= position && position < mAdapter.getCount()) {
				mAdapter.updatePosition(position, status);
				mAdapter.notifyDataSetChanged();
				final long accountId = GlobalContext.getCurrentAccount().user.id;
				final int group = ConfigManager.getAtmeFilter();
				MyAsyncTask.execute(new Runnable() {
					@Override
					public void run() {
						DatabaseUtils.updateAtmeStatus(status, position, accountId, group);
					}
				});
			}
        }
    }
    
    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }
    
    @Override
    List<WeiboStatus> getBeansFromDatabase(long accountId, int group) {
        return DatabaseUtils.getAtmeStatuses(accountId, group);
    }
    
    @Override
    void saveRefreshResultToDatabase(List<WeiboStatus> statuses, long accountId, int group) {
        DatabaseUtils.removeAtmeStatuses(accountId, group);
        DatabaseUtils.insertAtmeStatuses(statuses, accountId, group);
    }
    
    @Override
    void saveLoadMoreResultToDatabase(SparseArray<WeiboStatus> statuses, long accountId, int group) {
        DatabaseUtils.insertAtmeStatuses(statuses, accountId, group);
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        MentionsWeiboTimelineDao dao = new MentionsWeiboTimelineDao();
        dao.setFilter(ConfigManager.getAtmeFilter());
        return dao;
    }
    
    @Override
    public void saveListPosition() {
        View view = mListView.getChildAt(0);
        if (view != null) {
            final TimelinePosition position = new TimelinePosition();
            position.position = mListView.getFirstVisiblePosition();
            position.top = mListView.getChildAt(0).getTop();
            final long accountId = GlobalContext.getCurrentAccount().user.id;
            final int filter = ConfigManager.getAtmeFilter();
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils.insertOrUpdateTimelinePosition(position, MainActivity.ATME_LIST, filter, accountId);
                }
            });
            
        }
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = ActivityUtils.weiboActivity(mAdapter.getItem(position));
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
                        DatabaseUtils.removeAtmeStatus(position, accountId, group);
                    }
                });
            }
            
            @Override
            protected void updateDatabase(final WeiboStatus status, final int position, final long accountId,
                    final int group) {
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseUtils.updateAtmeStatus(status, position, accountId, group);
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
        return new MainRefreshTask();
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new MainLoadMoreTask();
    }
    
    @Override
    TimelinePosition onRestoreListPosition(int group) {
        return DatabaseUtils.getTimelinePosition(MainActivity.ATME_LIST, group);
    }
    
    @Override
    protected int getGroup() {
        return ConfigManager.getAtmeFilter();
    }
    
    private class OnMultiPictureClickListener implements WeiboListAdapter.OnMultiPictureClickListener {
        @Override
        public void onMultiPictureClick(int position, int picIndex) {
            WeiboStatus status = mAdapter.getItem(position);
            if (status.retweetStatus != null) {
                status = status.retweetStatus;
            }
            startActivity(ActivityUtils.imageViewerActivity(status, picIndex));
        }
    }
    
    private class OnPictureClickListener implements WeiboListAdapter.OnPictureClickListener {
        @Override
        public void onPictureClick(int position) {
            WeiboStatus status = mAdapter.getItem(position);
            if (status.retweetStatus != null) {
                status = status.retweetStatus;
            }
            startActivity(ActivityUtils.imageViewerActivity(status, 0));
        }
    }
}
