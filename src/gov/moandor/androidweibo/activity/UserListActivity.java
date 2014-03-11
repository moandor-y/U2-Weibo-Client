package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.AbsFriendsUserListFragment;
import gov.moandor.androidweibo.fragment.AbsUserListFragment;
import gov.moandor.androidweibo.fragment.FollowerListFragment;
import gov.moandor.androidweibo.fragment.FollowingListFragment;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;

public class UserListActivity extends AbsSwipeBackActivity implements PullToRefreshAttacherOwner {
    public static final String TYPE;
    public static final String USER;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        TYPE = packageName + ".type";
        USER = packageName + ".user";
    }
    
    private AbsFriendsUserListFragment mFragment;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        Type type = (Type) getIntent().getSerializableExtra(TYPE);
        WeiboUser user = getIntent().getParcelableExtra(USER);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (AbsFriendsUserListFragment) fragmentManager.findFragmentById(R.id.content);
        if (mFragment == null) {
            Bundle args = new Bundle();
            args.putLong(AbsUserListFragment.USER_ID, user.id);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (type) {
            case FOLLOWING:
                mFragment = new FollowingListFragment();
                mFragment.setArguments(args);
                fragmentTransaction.add(R.id.content, mFragment);
                break;
            case FOLLOWERS:
                mFragment = new FollowerListFragment();
                mFragment.setArguments(args);
                fragmentTransaction.add(R.id.content, mFragment);
            }
            fragmentTransaction.commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (type) {
        case FOLLOWING:
            getSupportActionBar().setTitle(getString(R.string.following_of, user.name));
            break;
        case FOLLOWERS:
            getSupportActionBar().setTitle(getString(R.string.followers_of, user.name));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_list, menu);
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
    
    public static enum Type {
        FOLLOWING, FOLLOWERS
    }
}
