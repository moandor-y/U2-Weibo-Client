package gov.moandor.androidweibo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.AbsBaseAdapter;
import gov.moandor.androidweibo.adapter.ISelectableAdapter;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.filter.AbsWeiboTextFilter;
import gov.moandor.androidweibo.util.filter.KeywordWeiboFilter;
import gov.moandor.androidweibo.util.filter.SourceFilter;
import gov.moandor.androidweibo.util.filter.UserWeiboFilter;
import gov.moandor.androidweibo.util.filter.WeiboFilter;

public class IgnoreActivity extends AbsActivity {
    private static final String TAG_FILTER_TYPE_DIALOG = "filter_type_dialog";
    private static final String TAG_EDIT_FILTER_DIALOG = "edit_filter_dialog";

    private IgnoreFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (IgnoreFragment) fm.findFragmentById(android.R.id.content);
        if (mFragment == null) {
            mFragment = new IgnoreFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(android.R.id.content, mFragment);
            ft.commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ignore);
    }

    private void onEditFilterFinished(final WeiboFilter filter) {
        if (filter == null) {
            return;
        }
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseUtils.insertOrUpdateWeiboFilter(filter);
                GlobalContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFragment.refresh();
                    }
                });
            }
        });
    }

    public static class IgnoreFragment extends Fragment implements AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener {
        private WeiboFilter[] mFilters = new WeiboFilter[0];
        private ActionMode mActionMode;
        private FilterListAdapter mAdapter = new FilterListAdapter();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
            refresh();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ListView list = new ListView(getActivity());
            list.setAdapter(mAdapter);
            list.setOnItemClickListener(this);
            list.setOnItemLongClickListener(this);
            return list;
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
                    dialog.show(getFragmentManager(), TAG_FILTER_TYPE_DIALOG);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle args = new Bundle();
            WeiboFilter filter = mFilters[position];
            if (filter instanceof KeywordWeiboFilter) {
                args.putSerializable(FilterDialogFragment.TYPE, FilterDialogFragment.Type.KEYWORD);
                args.putSerializable(FilterDialogFragment.FILTER, (KeywordWeiboFilter) filter);
            } else if (filter instanceof UserWeiboFilter) {
                args.putSerializable(FilterDialogFragment.TYPE, FilterDialogFragment.Type.USER);
                args.putSerializable(FilterDialogFragment.FILTER, (UserWeiboFilter) filter);
            } else if (filter instanceof SourceFilter) {
                args.putSerializable(FilterDialogFragment.TYPE, FilterDialogFragment.Type.SOURCE);
                args.putSerializable(FilterDialogFragment.FILTER, (SourceFilter) filter);
            }
            FilterDialogFragment dialog = new FilterDialogFragment();
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), TAG_EDIT_FILTER_DIALOG);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActionMode != null || position < 0 || mAdapter.getCount() <= position) {
                return false;
            }
            mAdapter.setSelectedPosition(position);
            mAdapter.notifyDataSetChanged();
            mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
            return true;
        }

        private void refresh() {
            new RefreshTask().execute();
        }

        private class RefreshTask extends MyAsyncTask<Void, Void, WeiboFilter[]> {
            @Override
            protected WeiboFilter[] doInBackground(Void... params) {
                return DatabaseUtils.getWeiboFilters();
            }

            @Override
            protected void onPostExecute(WeiboFilter[] result) {
                mFilters = result;
                mAdapter.notifyDataSetChanged();
            }
        }

        private class DeleteTask extends MyAsyncTask<Void, Void, Void> {
            private int mId;

            private DeleteTask(int id) {
                mId = id;
            }

            @Override
            protected Void doInBackground(Void... params) {
                DatabaseUtils.removeWeiboFilter(mId);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                refresh();
            }
        }

        private class FilterListAdapter extends AbsBaseAdapter implements ISelectableAdapter<WeiboFilter> {
            private int mSelectedPosition = -1;

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
                TextView textView;
                if (convertView != null) {
                    textView = (TextView) convertView;
                } else {
                    textView = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                    textView.setTextSize(mFontSize);
                }
                textView.setText(mFilters[position].toString());
                if (position == mSelectedPosition) {
                    textView.setBackgroundResource(R.color.ics_blue_semi);
                } else {
                    textView.setBackgroundResource(0);
                }
                return textView;
            }

            @Override
            public void setSelectedPosition(int position) {
                mSelectedPosition = position;
            }

            @Override
            public WeiboFilter getSelectedItem() {
                return mFilters[mSelectedPosition];
            }

            @Override
            public int getSelection() {
                return mSelectedPosition;
            }
        }

        private class ActionModeCallback implements ActionMode.Callback {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.long_click_filter, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        delete();
                        break;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                mAdapter.setSelectedPosition(-1);
                mAdapter.notifyDataSetChanged();
            }

            private void delete() {
                new DeleteTask(mAdapter.getSelectedItem().getId()).execute();
            }
        }
    }

    public static class FilterTypeDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.filter_types, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FilterDialogFragment.Type type = null;
                    switch (which) {
                        case 0:
                            type = FilterDialogFragment.Type.KEYWORD;
                            break;
                        case 1:
                            type = FilterDialogFragment.Type.USER;
                            break;
                        case 2:
                            type = FilterDialogFragment.Type.SOURCE;
                            break;
                    }
                    assert type != null;
                    Bundle args = new Bundle();
                    args.putSerializable(FilterDialogFragment.TYPE, type);
                    FilterDialogFragment df = new FilterDialogFragment();
                    df.setArguments(args);
                    df.show(getFragmentManager(), TAG_EDIT_FILTER_DIALOG);
                }
            });
            return builder.create();
        }
    }

    public static class FilterDialogFragment extends DialogFragment {
        public static final String TYPE = "type";
        public static final String FILTER = "filter";

        private void buildView(View view, AbsWeiboTextFilter filter) {
            if (filter != null) {
                CheckBox checkReposted = (CheckBox) view.findViewById(R.id.check_reposted);
                CheckBox isRegex = (CheckBox) view.findViewById(R.id.is_regex);
                EditText pattern = (EditText) view.findViewById(R.id.pattern);
                checkReposted.setChecked(filter.getCheckReposted());
                isRegex.setChecked(filter.isRegex());
                pattern.setText(filter.getPattern());
                if (filter.getId() >= 0) {
                    view.setTag(filter.getId());
                }
            }
        }        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Type type = (Type) getArguments().getSerializable(TYPE);
            int titleResId = 0;
            int layoutResId = 0;
            switch (type) {
                case KEYWORD:
                    titleResId = R.string.keywords;
                    layoutResId = R.layout.dialog_filter_keyword;
                    break;
                case USER:
                    titleResId = R.string.user;
                    layoutResId = R.layout.dialog_filter_user;
                    break;
                case SOURCE:
                    titleResId = R.string.source;
                    layoutResId = R.layout.dialog_filter_keyword;
                    break;
            }
            builder.setTitle(titleResId);
            final View view = getActivity().getLayoutInflater().inflate(layoutResId, null);
            switch (type) {
                case KEYWORD:
                case SOURCE:
                    buildView(view, (AbsWeiboTextFilter) getArguments().getSerializable(FILTER));
                    break;
                case USER:
                    buildView(view, (UserWeiboFilter) getArguments().getSerializable(FILTER));
                    break;
            }
            builder.setView(view);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WeiboFilter filter = null;
                    switch (type) {
                        case KEYWORD:
                            filter = new KeywordWeiboFilter();
                            filter = buildTextWeiboFilter(view, (AbsWeiboTextFilter) filter);
                            break;
                        case USER:
                            filter = new UserWeiboFilter();
                            filter = buildUserWeiboFilter(view, (UserWeiboFilter) filter);
                            break;
                        case SOURCE:
                            filter = new SourceFilter();
                            filter = buildTextWeiboFilter(view, (AbsWeiboTextFilter) filter);
                    }
                    ((IgnoreActivity) getActivity()).onEditFilterFinished(filter);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }

        private void buildView(View view, UserWeiboFilter filter) {
            if (filter != null) {
                RadioButton userName = (RadioButton) view.findViewById(R.id.user_name);
                RadioButton userId = (RadioButton) view.findViewById(R.id.user_id);
                EditText pattern = (EditText) view.findViewById(R.id.pattern);
                if (filter.getUser() != null) {
                    pattern.setText(filter.getUser().name);
                } else if (!TextUtils.isEmpty(filter.getUserName())) {
                    userName.toggle();
                    pattern.setText(filter.getUserName());
                } else {
                    userId.toggle();
                    pattern.setText(String.valueOf(filter.getUserId()));
                }
                if (filter.getId() >= 0) {
                    view.setTag(filter.getId());
                }
            }
        }

        private AbsWeiboTextFilter buildTextWeiboFilter(View view, AbsWeiboTextFilter filter) {
            CheckBox checkReposted = (CheckBox) view.findViewById(R.id.check_reposted);
            CheckBox isRegex = (CheckBox) view.findViewById(R.id.is_regex);
            EditText pattern = (EditText) view.findViewById(R.id.pattern);
            if (!TextUtils.isEmpty(pattern.getText())) {
                filter.setPattern(pattern.getText().toString());
                filter.setCheckReposted(checkReposted.isChecked());
                filter.setIsRegex(isRegex.isChecked());
                Integer id = (Integer) view.getTag();
                if (id != null) {
                    filter.setId(id);
                }
                return filter;
            } else {
                return null;
            }
        }

        private UserWeiboFilter buildUserWeiboFilter(View view, UserWeiboFilter filter) {
            RadioButton userName = (RadioButton) view.findViewById(R.id.user_name);
            RadioButton userId = (RadioButton) view.findViewById(R.id.user_id);
            EditText pattern = (EditText) view.findViewById(R.id.pattern);
            if (!TextUtils.isEmpty(pattern.getText())) {
                Integer id = (Integer) view.getTag();
                if (id != null) {
                    filter.setId(id);
                }
                if (userName.isChecked()) {
                    filter.setUserName(pattern.getText().toString());
                    return filter;
                } else if (userId.isChecked()) {
                    try {
                        filter.setUserId(Long.parseLong(pattern.getText().toString()));
                        return filter;
                    } catch (NumberFormatException e) {
                        Logger.logException(e);
                    }
                }
            }
            return null;
        }

        private static enum Type {
            KEYWORD, USER, SOURCE
        }


    }
}
