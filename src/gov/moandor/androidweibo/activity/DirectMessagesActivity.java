package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.DirectMessagesUserListFragment;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;

public class DirectMessagesActivity extends AbsActivity implements PullToRefreshAttacherOwner {
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private DirectMessagesUserListFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_user_list);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (DirectMessagesUserListFragment) fragmentManager.findFragmentById(R.id.content);
        if (mFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mFragment = new DirectMessagesUserListFragment();
            fragmentTransaction.add(R.id.content, mFragment);
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
    
    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }
}
