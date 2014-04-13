package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.AuthorizeActivity;
import gov.moandor.androidweibo.activity.DirectMessagesActivity;
import gov.moandor.androidweibo.activity.DraftBoxActivity;
import gov.moandor.androidweibo.activity.FavoritesActivity;
import gov.moandor.androidweibo.adapter.MainDrawerListAdapter;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

public class MainDrawerFragment extends Fragment implements AdapterView.OnItemClickListener {
    private MainDrawerListAdapter mAdapter;
    private View mFooterView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_main, container, false);
        ListView list = (ListView) view.findViewById(R.id.account_list);
        mFooterView = inflater.inflate(R.layout.account_list_footer, list, false);
        list.addFooterView(mFooterView);
        mAdapter = new MainDrawerListAdapter();
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);
        registerForContextMenu(list);
        view.findViewById(R.id.draft_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), DraftBoxActivity.class);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), FavoritesActivity.class);
                startActivity(intent);
            }
        });
        Button directMessages = (Button) view.findViewById(R.id.direct_messages);
        if (Utilities.isBmEnabled()) {
            directMessages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(GlobalContext.getInstance(), DirectMessagesActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            directMessages.setVisibility(View.GONE);
        }
        return view;
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view != mFooterView) {
            if (position != GlobalContext.getCurrentAccountIndex()) {
                ((OnAccountClickListener) getActivity()).onAccountClick(position);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), AuthorizeActivity.class);
            getActivity().startActivity(intent);
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        if (position < GlobalContext.getAccountCount()) {
            getActivity().getMenuInflater().inflate(R.menu.main_drawer_context_menu, menu);
            menu.setHeaderTitle(GlobalContext.getAccount(((AdapterView.AdapterContextMenuInfo) menuInfo).position).user.name);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.logout:
            GlobalContext.removeAccount(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
            if (GlobalContext.getCurrentAccount() != null) {
                mAdapter.notifyDataSetChanged();
                ((OnAccountClickListener) getActivity()).onAccountClick(GlobalContext.getCurrentAccountIndex());
            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AuthorizeActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
            return true;
        }
        return false;
    }
    
    public static interface OnAccountClickListener {
        public void onAccountClick(int position);
    }
}
