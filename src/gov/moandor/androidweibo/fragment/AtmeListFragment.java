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
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.MentionsWeiboTimelineDao;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;
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
            final int position = data.getIntExtra(WeiboActivity.POSITION, -1);
            final WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            mAdapter.updatePosition(position, status);
            mAdapter.notifyDataSetChanged();
            final long accountId = GlobalContext.getCurrentAccount().user.id;
            final int group = GlobalContext.getWeiboGroup();
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils.updateAtmeStatus(status, position, accountId, group);
                }
            });
        }
    }
    
    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }
    
    @Override
    List<WeiboStatus> getBeansFromJson(String json) throws WeiboException {
        return JsonUtils.getWeiboStatusesFromJson(json);
    }
    
    @Override
    List<WeiboStatus> getBeansFromDatabase(long accountId, int group) {
        return DatabaseUtils.getAtmeStatuses(accountId, group);
    }
    
    @Override
    void saveRefreshResultToDatabase(List<WeiboStatus> statuses) {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int filter = GlobalContext.getAtmeFilter();
        DatabaseUtils.removeAtmeStatuses(accountId, filter);
        DatabaseUtils.insertAtmeStatuses(statuses, accountId, filter);
    }
    
    @Override
    void saveLoadMoreResultToDatabase(SparseArray<WeiboStatus> statuses) {
        long accountId = GlobalContext.getCurrentAccount().user.id;
        int filter = GlobalContext.getAtmeFilter();
        DatabaseUtils.insertAtmeStatuses(statuses, accountId, filter);
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        MentionsWeiboTimelineDao dao = new MentionsWeiboTimelineDao();
        dao.setFilter(GlobalContext.getAtmeFilter());
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
            final int filter = GlobalContext.getAtmeFilter();
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
    TimelinePosition onRestoreListPosition() {
        return DatabaseUtils.getTimelinePosition(MainActivity.ATME_LIST, GlobalContext.getAtmeFilter());
    }
	
	@Override
	protected int getGroup() {
		return GlobalContext.getAtmeFilter();
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
}
