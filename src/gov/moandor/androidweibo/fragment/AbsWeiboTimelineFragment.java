package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;

import java.util.List;

public abstract class AbsWeiboTimelineFragment<DataBean extends AbsItemBean, TimelineListAdapter extends AbsTimelineListAdapter<DataBean>>
        extends AbsTimelineFragment<DataBean, TimelineListAdapter> {
    EditText mQuickPost;
    ImageButton mSendButton;
    private boolean mFirstShown = true;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weibo_timeline, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQuickPost = (EditText) view.findViewById(R.id.quick_post);
        mSendButton = (ImageButton) view.findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new SendButtonOnClickListener());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();
    }
    
    @Override
    boolean isThisCurrentFragment() {
        WeiboActivity activity = (WeiboActivity) getActivity();
        if (activity != null) {
            return activity.isCurrentFragment(this);
        }
        return false;
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return new WeiboTimelineLoadMoreTask();
    }
    
    @Override
    RefreshTask createRefreshTask() {
        return new WeiboTimelineRefreshTask();
    }
    
    public void onShown() {
        if (mFirstShown) {
            mFirstShown = false;
            refresh();
        }
        hideKeyboard();
    }
    
    private void hideKeyboard() {
        mQuickPost.clearFocus();
        Utilities.hideKeyboard(mQuickPost.getWindowToken());
    }
    
    private class SendButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String content = mQuickPost.getText().toString();
            if (TextUtils.isEmpty(content)) {
                return;
            } else if (Utilities.sendLength(content) >= 140) {
                return;
            }
            mQuickPost.setText(null);
            hideKeyboard();
            onSend(content);
        }
    }
    
    private class WeiboTimelineRefreshTask extends RefreshTask {
        @Override
        protected void onPostExecute(List<DataBean> result) {
            super.onPostExecute(result);
            hideKeyboard();
        }
    }
    
    private class WeiboTimelineLoadMoreTask extends LoadMoreTask {
        @Override
        protected void onPostExecute(List<DataBean> result) {
            super.onPostExecute(result);
            hideKeyboard();
        }
    }
    
    abstract void onSend(String content);
}
