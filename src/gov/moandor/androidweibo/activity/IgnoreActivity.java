package gov.moandor.androidweibo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.KeywordWeiboFilter;
import gov.moandor.androidweibo.bean.WeiboFilter;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public class IgnoreActivity extends AbsActivity {
    private static final String TAG_FILTER_TYPE_DIALOG = "filter_type_dialog";
    private static final String TAG_ADD_FILTER_DIALOG = "add_filter_dialog";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(android.R.id.content);
        if (fragment == null) {
            fragment = new IgnoreFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(android.R.id.content, fragment);
            ft.commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ignore);
    }
    
    public static class IgnoreFragment extends ListFragment implements FilterTypeDialogFragment.OnAddFilterListener {
        private WeiboFilter[] mFilters = new WeiboFilter[0];
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
            setListAdapter(new ListAdapter());
            new RefreshTask().execute();
        }
        
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_filter, menu);
        }
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.add:
                FilterTypeDialogFragment dialog = new FilterTypeDialogFragment();
                dialog.setOnAddFilterListener(this);
                dialog.show(getFragmentManager(), TAG_FILTER_TYPE_DIALOG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
        }
        
        @Override
        public void onAddFilter(final WeiboFilter filter) {
            MyAsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DatabaseUtils.insertWeiboFilter(filter);
                    GlobalContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new RefreshTask().execute();
                        }
                    });
                }
            });
        }
        
        private class RefreshTask extends MyAsyncTask<Void, Void, WeiboFilter[]> {
            @Override
            protected WeiboFilter[] doInBackground(Void... params) {
                return DatabaseUtils.getWeiboFilters();
            }
            
            @Override
            protected void onPostExecute(WeiboFilter[] result) {
                mFilters = result;
                ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
            }
        }
        
        private class ListAdapter extends BaseAdapter {
            @Override
            public int getCount() {
                return mFilters.length;
            }
            
            @Override
            public WeiboFilter getItem(int position) {
                return mFilters[position];
            }
            
            @Override
            public long getItemId(int position) {
                return mFilters[position].getId();
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = null;
                if (convertView != null) {
                    textView = (TextView) convertView;
                } else {
                    textView = (TextView) getActivity().getLayoutInflater().inflate(
                            android.R.layout.simple_list_item_1, parent, false);
                }
                textView.setText(mFilters[position].toString());
                return textView;
            }
        }
    }
    
    public static class FilterTypeDialogFragment extends DialogFragment {
        private OnAddFilterListener mListener;
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.filter_types, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    case 0:
                        showFilterDialog(R.string.keywords, R.layout.dialog_filter_keyword, 
                                new OnKeywordFilterFinishedListener());
                        break;
                    }
                }
            });
            return builder.create();
        }
        
        public void setOnAddFilterListener(OnAddFilterListener l) {
            mListener = l;
        }
        
        private void showFilterDialog(int titleResId, int layoutResId, 
                FilterDialogFragment.OnFinishedListener listener) {
            FilterDialogFragment dialog = new FilterDialogFragment();
            dialog.setOnFinishedListener(listener);
            Bundle args = new Bundle();
            args.putInt(FilterDialogFragment.TITLE_RES_ID, titleResId);
            args.putInt(FilterDialogFragment.LAYOUT_RES_ID, layoutResId);
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), TAG_ADD_FILTER_DIALOG);
        }
        
        private class OnKeywordFilterFinishedListener implements FilterDialogFragment.OnFinishedListener {
            @Override
            public void onFinished(View view) {
                CheckBox checkReposted = (CheckBox) view.findViewById(R.id.check_reposted);
                CheckBox isRegex = (CheckBox) view.findViewById(R.id.is_regex);
                EditText pattern = (EditText) view.findViewById(R.id.pattern);
                if (!TextUtils.isEmpty(pattern.getText()) && mListener != null) {
                    KeywordWeiboFilter filter = new KeywordWeiboFilter();
                    filter.setPattern(pattern.getText().toString());
                    filter.setCheckReposted(checkReposted.isChecked());
                    filter.setIsRegex(isRegex.isChecked());
                    mListener.onAddFilter(filter);
                }
            }
        }
        
        public static interface OnAddFilterListener {
            public void onAddFilter(WeiboFilter filter);
        }
    }
    
    public static class FilterDialogFragment extends DialogFragment {
        public static final String TITLE_RES_ID = "title_res_id";
        public static final String LAYOUT_RES_ID = "layout_res_id";
        
        private View mView;
        private OnFinishedListener mListener;
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getArguments().getInt(TITLE_RES_ID));
            mView = getActivity().getLayoutInflater().inflate(
                    getArguments().getInt(LAYOUT_RES_ID), null);
            builder.setView(mView);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) {
                        mListener.onFinished(mView);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
        
        public void setOnFinishedListener(OnFinishedListener l) {
            mListener = l;
        }
        
        public static interface OnFinishedListener {
            public void onFinished(View view);
        }
    }
}
