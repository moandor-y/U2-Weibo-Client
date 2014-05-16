package gov.moandor.androidweibo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboTimelineListAdapter;
import gov.moandor.androidweibo.bean.WeiboDraft;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.RepostTimelineDao;
import gov.moandor.androidweibo.notification.SendWeiboService;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.WeiboListActionModeCallback;

public class WeiboRepostListFragment extends
        AbsWeiboTimelineFragment<WeiboStatus, WeiboTimelineListAdapter<WeiboStatus>> {
    private WeiboStatus mWeiboStatus;
    private View mQuickRepostLayout;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((WeiboActivity) getActivity()).setWeiboRepostListFragment(this);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQuickRepostLayout = view.findViewById(R.id.quick_post_layout);
        mQuickPost.setHint(R.string.quick_repost);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeiboStatus = ((WeiboActivity) getActivity()).getWeiboStatus();
        if (mWeiboStatus.retweetStatus != null) {
            mQuickRepostLayout.setVisibility(View.GONE);
        }
    }
    
    @Override
    WeiboTimelineListAdapter<WeiboStatus> createListAdapter() {
        return new WeiboTimelineListAdapter<WeiboStatus>();
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboStatus> onCreateDao() {
        RepostTimelineDao dao = new RepostTimelineDao();
        dao.setWeiboId(mWeiboStatus.id);
        return dao;
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = ActivityUtils.weiboActivity(mAdapter.getItem(position));
        startActivity(intent);
    }
    
    @Override
    void onSend(String content) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), SendWeiboService.class);
        WeiboDraft draft = new WeiboDraft();
        draft.content = content;
        draft.retweetStatus = mWeiboStatus;
        draft.accountId = GlobalContext.getCurrentAccount().user.id;
        intent.putExtra(SendWeiboService.TOKEN, GlobalContext.getCurrentAccount().token);
        intent.putExtra(SendWeiboService.WEIBO_DRAFT, draft);
        getActivity().startService(intent);
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        WeiboListActionModeCallback callback = new WeiboListActionModeCallback();
        callback.setAdapter(mAdapter);
        callback.setFragment(this);
        return callback;
    }
}
