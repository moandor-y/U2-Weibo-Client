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
import gov.moandor.androidweibo.dao.SearchTopicsDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

public class TopicWeiboListFragment extends AbsTimelineFragment<WeiboStatus, WeiboListAdapter> {
    public static final String TOPIC = "topic";
    private static final int REQUEST_CODE = 0;

    private String mTopic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTopic = getArguments().getString(TOPIC);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter.getCount() == 0) {
            refresh();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setOnPictureClickListener(new OnPictureClickListener());
        mAdapter.setOnMultiPictureClickListener(new OnMultiPictureClickListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            int position = mAdapter.positionOf(status.id);
            mAdapter.updatePosition(position, status);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }

    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        SearchTopicsDao dao = new SearchTopicsDao();
        dao.setTopic(mTopic);
        return dao;
    }

    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = ActivityUtils.weiboActivity(mAdapter.getItem(position));
        startActivityForResult(intent, REQUEST_CODE);
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
