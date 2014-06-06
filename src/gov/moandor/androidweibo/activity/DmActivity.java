package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.DmConversationFragment;
import gov.moandor.androidweibo.fragment.DmUserListFragment;
import gov.moandor.androidweibo.util.Utilities;

public class DmActivity extends AbsActivity {
    public static final String FROM_UNREAD = Utilities.buildIntentExtraName("FROM_UNREAD");
    
    private DmUserListFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (DmUserListFragment) fragmentManager.findFragmentById(android.R.id.content);
        if (mFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mFragment = new DmUserListFragment();
            boolean fromUnread = getIntent().getBooleanExtra(FROM_UNREAD, false);
            if (fromUnread) {
                Bundle args = new Bundle();
                args.putBoolean(DmUserListFragment.FROM_UNREAD, true);
                mFragment.setArguments(args);
            }
            fragmentTransaction.add(android.R.id.content, mFragment);
            fragmentTransaction.commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.direct_messages);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dm_user_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.refresh:
            mFragment.refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public static class ConversationActivity extends AbsActivity {
        public static final String USER = Utilities.buildIntentExtraName("USER");
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            WeiboUser user = getIntent().getParcelableExtra(USER);
            FragmentManager fragmentManager = getSupportFragmentManager();
            DmConversationFragment fragment =
                    (DmConversationFragment) fragmentManager.findFragmentById(android.R.id.content);
            if (fragment == null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragment = new DmConversationFragment();
                Bundle args = new Bundle();
                args.putParcelable(DmConversationFragment.USER, user);
                fragment.setArguments(args);
                fragmentTransaction.add(android.R.id.content, fragment);
                fragmentTransaction.commit();
            }
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(user.name);
        }
    }
}
