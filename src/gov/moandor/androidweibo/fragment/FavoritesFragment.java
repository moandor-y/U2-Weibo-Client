package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.activity.ImageViewerActivity;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboListAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.FavoritesDao;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

import java.util.List;

public class FavoritesFragment extends AbsTimelineFragment<WeiboStatus, WeiboListAdapter> {
    private static final int REQUEST_CODE = 0;
    
    private boolean mNoMore;
    private int mPage = 1;
    
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
            int position = data.getIntExtra(WeiboActivity.POSITION, -1);
            WeiboStatus status = data.getParcelableExtra(WeiboActivity.WEIBO_STATUS);
            mAdapter.updatePosition(position, status);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    WeiboListAdapter createListAdapter() {
        return new WeiboListAdapter();
    }
    
    @Override
    List<WeiboStatus> getBeansFromJson(String json) throws WeiboException {
        return Utilities.getFavoritesFromJson(json);
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        FavoritesDao dao = new FavoritesDao();
        dao.setPage(mPage);
        return dao;
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
    boolean isThisCurrentFragment() {
        return true;
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new FavoritesLoadMoreTask();
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new FavoritesRefreshTask();
    }
    
    @Override
    void loadMore() {
        if (!mNoMore) {
            super.loadMore();
        }
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
    
    private class FavoritesRefreshTask extends RefreshTask {
        @Override
        protected void onPreExecute() {
            mNoMore = false;
            mPage = 1;
        }
        
        @Override
        protected void onPostExecute(List<WeiboStatus> result) {
            mAdapter.clearDataSet();
            super.onPostExecute(result);
        }
    }
    
    private class FavoritesLoadMoreTask extends LoadMoreTask {
        @Override
        protected void onPreExecute() {
            mPage += 1;
        }
        
        @Override
        protected void onPostExecute(List<WeiboStatus> result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result.size() == 0) {
                    mNoMore = true;
                }
            }
        }
    }
}
