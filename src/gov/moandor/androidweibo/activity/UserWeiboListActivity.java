package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.UserWeiboListFragment;
import gov.moandor.androidweibo.util.GlobalContext;

public class UserWeiboListActivity extends AbsActivity {
    public static final String USER;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        USER = packageName + ".user";
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_weibo_list);
        WeiboUser user = getIntent().getParcelableExtra(USER);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            Fragment fragment = new UserWeiboListFragment();
            Bundle args = new Bundle();
            args.putLong(UserWeiboListFragment.USER_ID, user.id);
            fragment.setArguments(args);
            fm.beginTransaction().add(R.id.content, fragment).commit();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.posts_of, user.name));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
