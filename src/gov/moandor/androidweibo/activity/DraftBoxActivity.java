package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsDraftBean;
import gov.moandor.androidweibo.bean.CommentDraft;
import gov.moandor.androidweibo.bean.WeiboDraft;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.notification.SendCommentService;
import gov.moandor.androidweibo.notification.SendWeiboService;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;

public class DraftBoxActivity extends AbsActivity {
    private ListView mListView;
    private ListAdapter mListAdapter;
    private List<AbsDraftBean> mBeans = new ArrayList<AbsDraftBean>();
    private MyAsyncTask<Void, Void, List<AbsDraftBean>> mTask;
    private ActionMode mActionMode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_box);
        mListView = (ListView) findViewById(R.id.list);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener());
        mListView.setOnItemLongClickListener(new OnItemLongClickListener());
        mTask = new GetDraftsTask();
        mTask.execute();
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.draft_box);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class ListAdapter extends BaseAdapter {
        private Set<Integer> mCheckedPositions = new HashSet<Integer>();
        
        @Override
        public int getCount() {
            return mBeans.size();
        }
        
        @Override
        public AbsDraftBean getItem(int position) {
            return mBeans.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return mBeans.get(position).id;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.draft_list_item, parent, false);
            }
            TextView content = (TextView) convertView.findViewById(R.id.content);
            content.setText(mBeans.get(position).content);
            if (mCheckedPositions.contains(position)) {
                convertView.setBackgroundResource(R.color.ics_blue_semi);
            } else {
                convertView.setBackgroundResource(0);
            }
            return convertView;
        }
        
        public void check(int position) {
            mCheckedPositions.add(position);
        }
        
        public void uncheck(int position) {
            mCheckedPositions.remove(position);
        }
        
        public void clearCheck() {
            mCheckedPositions.clear();
        }
        
        public boolean isChecked(int position) {
            return mCheckedPositions.contains(position);
        }
        
        public int getCheckedCount() {
            return mCheckedPositions.size();
        }
        
        public int[] getCheckedIds() {
            int count = mCheckedPositions.size();
            Integer[] positions = mCheckedPositions.toArray(new Integer[0]);
            int[] result = new int[count];
            for (int i = 0; i < count; i++) {
                result[i] = (int) getItemId(positions[i]);
            }
            return result;
        }
        
        public AbsDraftBean[] getCheckedItems() {
            int count = mCheckedPositions.size();
            Integer[] positions = mCheckedPositions.toArray(new Integer[0]);
            AbsDraftBean[] result = new AbsDraftBean[count];
            for (int i = 0; i < count; i++) {
                result[i] = getItem(positions[i]);
            }
            return result;
        }
    }
    
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (mTask != null && mTask.getStatus() != MyAsyncTask.Status.FINISHED) {
                return true;
            }
            switch (item.getItemId()) {
            case R.id.send:
                for (AbsDraftBean draft : mListAdapter.getCheckedItems()) {
                    Intent intent = new Intent();
                    if (draft instanceof WeiboDraft) {
                        intent.setClass(GlobalContext.getInstance(), SendWeiboService.class);
                        intent.putExtra(SendWeiboService.TOKEN, GlobalContext.getCurrentAccount().token);
                        intent.putExtra(SendWeiboService.WEIBO_DRAFT, draft);
                    } else if (draft instanceof CommentDraft) {
                        intent.setClass(GlobalContext.getInstance(), SendCommentService.class);
                        intent.putExtra(SendCommentService.TOKEN, GlobalContext.getCurrentAccount().token);
                        intent.putExtra(SendCommentService.COMMENT_DRAFT, draft);
                    }
                    startService(intent);
                }
                mTask = new RemoveAndGetDraftsTask(mListAdapter.getCheckedIds());
                mTask.execute();
                break;
            case R.id.delete:
                mTask = new RemoveAndGetDraftsTask(mListAdapter.getCheckedIds());
                mTask.execute();
                break;
            }
            mode.finish();
            return true;
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.draft_long_click, menu);
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mListAdapter.clearCheck();
            mListAdapter.notifyDataSetChanged();
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }
    };
    
    private class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActionMode != null) {
                if (mListAdapter.isChecked(position)) {
                    mListAdapter.uncheck(position);
                } else {
                    mListAdapter.check(position);
                }
                mListAdapter.notifyDataSetChanged();
                if (mListAdapter.getCheckedCount() == 0) {
                    mActionMode.finish();
                }
            } else if (mTask == null || mTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                AbsDraftBean draft = mListAdapter.getItem(position);
                Intent intent = new Intent();
                if (draft instanceof WeiboDraft) {
                    intent.setClass(GlobalContext.getInstance(), WriteWeiboActivity.class);
                    intent.putExtra(WriteWeiboActivity.DRAFT, draft);
                } else {
                    intent.setClass(GlobalContext.getInstance(), WriteCommentActivity.class);
                    intent.putExtra(WriteCommentActivity.DRAFT, draft);
                }
                startActivity(intent);
                mTask = new RemoveAndGetDraftsTask(draft.id);
                mTask.execute();
            }
        }
    }
    
    private class OnItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mActionMode = startSupportActionMode(mActionModeCallback);
            mListAdapter.check(position);
            mListAdapter.notifyDataSetChanged();
            return true;
        }
    }
    
    private class GetDraftsTask extends MyAsyncTask<Void, Void, List<AbsDraftBean>> {
        @Override
        protected List<AbsDraftBean> doInBackground(Void... params) {
            return DatabaseUtils.getDrafts(GlobalContext.getCurrentAccount().user.id);
        }
        
        @Override
        protected void onPostExecute(List<AbsDraftBean> result) {
            mBeans.clear();
            mBeans.addAll(result);
            mListAdapter.notifyDataSetChanged();
        }
    }
    
    private class RemoveAndGetDraftsTask extends MyAsyncTask<Void, Void, List<AbsDraftBean>> {
        private int[] mIds;
        
        public RemoveAndGetDraftsTask(int[] ids) {
            mIds = ids;
        }
        
        public RemoveAndGetDraftsTask(int id) {
            this(new int[]{id});
        }
        
        @Override
        protected List<AbsDraftBean> doInBackground(Void... params) {
            DatabaseUtils.removeDrafts(mIds);
            return DatabaseUtils.getDrafts(GlobalContext.getCurrentAccount().user.id);
        }
        
        @Override
        protected void onPostExecute(List<AbsDraftBean> result) {
            mBeans.clear();
            mBeans.addAll(result);
            mListAdapter.notifyDataSetChanged();
        }
    }
}
