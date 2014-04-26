package gov.moandor.androidweibo.fragment;

import android.os.Bundle;
import android.support.v7.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.DmConversationAdapter;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.dao.BaseTimelineJsonDao;
import gov.moandor.androidweibo.util.WeiboException;

public class DmConversationFragment extends AbsTimelineFragment<DirectMessage, DmConversationAdapter> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dm_conversation, container, false);
    }
    
    @Override
    DmConversationAdapter createListAdapter() {
        return new DmConversationAdapter();
    }
    
    @Override
    List<DirectMessage> getBeansFromJson(String json) throws WeiboException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected void loadMore() {
        // do nothing
    }
    
    @Override
    LoadMoreTask createLoadMoreTask() {
        return null;
    }
    
    @Override
    RefreshTask createRefreshTask() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
    }
    
    @Override
    Callback getActionModeCallback() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected BaseTimelineJsonDao<DirectMessage> onCreateDao() {
        // TODO Auto-generated method stub
        return null;
    }
}
