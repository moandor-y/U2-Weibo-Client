package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.UserWeiboListFragment;
import gov.moandor.androidweibo.util.GlobalContext;
import android.view.Menu;

public class UserWeiboListActivity extends AbsActivity {
    public static final String USER;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        USER = packageName + ".user";
    }
    
	private UserWeiboListFragment mFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeiboUser user = getIntent().getParcelableExtra(USER);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            mFragment = new UserWeiboListFragment();
            Bundle args = new Bundle();
            args.putLong(UserWeiboListFragment.USER_ID, user.id);
            mFragment.setArguments(args);
            fm.beginTransaction().add(android.R.id.content, mFragment).commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.posts_of, user.name));
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_weibo_list, menu);
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
}
