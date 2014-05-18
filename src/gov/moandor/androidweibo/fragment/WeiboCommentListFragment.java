package gov.moandor.androidweibo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.WeiboTimelineListAdapter;
import gov.moandor.androidweibo.bean.CommentDraft;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.dao.CommentsShowDao;
import gov.moandor.androidweibo.notification.SendCommentService;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.CommentListActionModeCallback;
import gov.moandor.androidweibo.util.GlobalContext;

public class WeiboCommentListFragment extends
        AbsWeiboTimelineFragment<WeiboComment, WeiboTimelineListAdapter<WeiboComment>> {
    private WeiboStatus mWeiboStatus;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((WeiboActivity) getActivity()).setWeiboCommentListFragment(this);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQuickPost.setHint(R.string.quick_comment);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeiboStatus = ((WeiboActivity) getActivity()).getWeiboStatus();
    }
    
    @Override
    WeiboTimelineListAdapter<WeiboComment> createListAdapter() {
        return new WeiboTimelineListAdapter<WeiboComment>();
    }
    
    @Override
    protected BaseTimelineJsonDao<WeiboComment> onCreateDao() {
        CommentsShowDao dao = new CommentsShowDao();
        dao.setWeiboId(mWeiboStatus.id);
        return dao;
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WeiboComment comment = mAdapter.getItem(position);
        startActivity(ActivityUtils.writeCommentActivity(comment.weiboStatus, comment));
    }
    
    @Override
    void onSend(String content) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), SendCommentService.class);
        intent.putExtra(SendCommentService.TOKEN, GlobalContext.getCurrentAccount().token);
        CommentDraft draft = new CommentDraft();
        draft.content = content;
        draft.commentedStatus = mWeiboStatus;
        draft.accountId = GlobalContext.getCurrentAccount().user.id;
        intent.putExtra(SendCommentService.COMMENT_DRAFT, draft);
        getActivity().startService(intent);
    }
    
    @Override
    ActionMode.Callback getActionModeCallback() {
        CommentListActionModeCallback callback = new CommentListActionModeCallback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.removeItem(R.id.view_weibo);
                return super.onPrepareActionMode(mode, menu);
            }
        };
        callback.setAdapter(mAdapter);
        callback.setFragment(this);
        return callback;
    }
}
