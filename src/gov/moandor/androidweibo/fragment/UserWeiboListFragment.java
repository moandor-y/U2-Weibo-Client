package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboListAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.UserTimelineDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

public class UserWeiboListFragment extends AbsTimelineFragment<WeiboStatus, WeiboListAdapter> {
    public static final String USER_ID = "user_id";
    private static final int REQUEST_CODE = 0;
    
    private long mUserId;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getLong(USER_ID);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setOnPictureClickListener(new OnPictureClickListener());
        mAdapter.setOnMultiPictureClickListener(new OnMultiPictureClickListener());
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter.getCount() == 0) {
            refresh();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            mAdapter.updatePosition(mAdapter.positionOf(status.id), status);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        UserTimelineDao dao = new UserTimelineDao();
        dao.setUserId(mUserId);
        return dao;
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new LoadMoreTask();
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new RefreshTask();
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = ActivityUtils.weiboActivity(mAdapter.getItem(position));
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        WeiboListActionModeCallback callback = new WeiboListActionModeCallback();
        callback.setAdapter(mAdapter);
        callback.setFragment(this);
        return callback;
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
