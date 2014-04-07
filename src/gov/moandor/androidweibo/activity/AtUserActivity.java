package gov.moandor.androidweibo.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.AtUserSuggestionsDao;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.ArrayList;
import java.util.List;

public class AtUserActivity extends AbsActivity {
    private ListAdapter mAdapter;
    private List<UserSuggestion> mSuggestions = new ArrayList<UserSuggestion>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_user);
        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new ListAdapter();
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_at_user, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.search_friends));
        searchView.setOnQueryTextListener(new OnQueryTextListener());
        searchView.requestFocus();
        return true;
    }
    
    private class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSuggestions.size();
        }
        
        @Override
        public Object getItem(int position) {
            return mSuggestions.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return mSuggestions.get(position).id;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.user_suggestion_item, parent, false);
            }
            TextView nameView = (TextView) convertView.findViewById(R.id.name);
            UserSuggestion suggestion = mSuggestions.get(position);
            String name;
            if (TextUtils.isEmpty(suggestion.remark)) {
                name = suggestion.nickname;
            } else {
                name = suggestion.nickname + " (" + suggestion.remark + ")";
            }
            nameView.setText(name);
            return convertView;
        }
    }
    
    private class OnQueryTextListener implements SearchView.OnQueryTextListener {
        private SearchTask mSearchTask;
        
        @Override
        public boolean onQueryTextChange(String newText) {
            if (!TextUtils.isEmpty(newText)) {
                if (mSearchTask != null && mSearchTask.getStatus() != MyAsyncTask.Status.FINISHED) {
                    mSearchTask.cancel(true);
                }
                mSearchTask = new SearchTask(newText);
                mSearchTask.execute();
            } else {
                mSuggestions.clear();
                mAdapter.notifyDataSetChanged();
            }
            return false;
        }
        
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    }
    
    private class SearchTask extends MyAsyncTask<Void, Void, List<UserSuggestion>> {
        private String mKeyword;
        
        public SearchTask(String keyword) {
            mKeyword = keyword;
        }
        
        @Override
        protected List<UserSuggestion> doInBackground(Void... v) {
            AtUserSuggestionsDao dao = new AtUserSuggestionsDao();
            dao.setKeyword(mKeyword);
            try {
                return dao.fetchData();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                cancel(true);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(List<UserSuggestion> result) {
            mSuggestions.clear();
            mSuggestions.addAll(result);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent data = new Intent();
            data.putExtra(AbsWriteActivity.AT_USER_RESULT_NAME, mSuggestions.get(position));
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
