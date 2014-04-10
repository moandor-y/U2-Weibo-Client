package gov.moandor.androidweibo.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.UserShowDao;
import gov.moandor.androidweibo.fragment.ProfileFragment;
import gov.moandor.androidweibo.fragment.ProgressDialogFragment;
import gov.moandor.androidweibo.util.FollowTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.UnfollowTask;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class UserActivity extends AbsActivity {
    public static final String USER;
    public static final String LOADING_DIALOG = "loading_dialog";
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        USER = packageName + ".user";
    }
    
    private WeiboUser mUser;
    private ProfileFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mUser = getIntent().getParcelableExtra(USER);
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (ProfileFragment) fm.findFragmentById(R.id.content);
        if (mFragment == null) {
            buildContent();
        } else {
            mUser = mFragment.getUser();
            getSupportActionBar().setTitle(mUser.name);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mUser == null) {
            return false;
        }
        if (mUser.following) {
            menu.findItem(R.id.follow).setVisible(false);
            if (mUser.followMe) {
                menu.findItem(R.id.followed_each_other).setVisible(true);
                menu.findItem(R.id.unfollow).setVisible(false);
            } else {
                menu.findItem(R.id.followed_each_other).setVisible(false);
                menu.findItem(R.id.unfollow).setVisible(true);
            }
        } else {
            menu.findItem(R.id.follow).setVisible(true);
            menu.findItem(R.id.unfollow).setVisible(false);
            menu.findItem(R.id.followed_each_other).setVisible(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.follow:
            new FollowTask(mUser, new OnFollowFinishedListener()).execute();
            return true;
        case R.id.unfollow:
        case R.id.followed_each_other:
            new UnfollowTask(mUser, new OnUnfollowFinishedListener()).execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(LOADING_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
    
    private void buildContent() {
        if (mUser != null) {
            onUserLoadFinished();
        } else {
            Uri data = getIntent().getData();
            DialogFragment dialog = ProgressDialogFragment.newInstance(getString(R.string.loading));
            dialog.show(getSupportFragmentManager(), LOADING_DIALOG);
            String userName = data.toString();
            int index = userName.lastIndexOf("@");
            userName = userName.substring(index + 1);
            LoadUserTask task = new LoadUserTask(userName);
            task.execute();
            getSupportActionBar().setTitle(R.string.user);
        }
    }
    
    private void onUserLoadFinished() {
        getSupportActionBar().setTitle(mUser.name);
        mFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(ProfileFragment.USER, mUser);
        mFragment.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        try {
            fm.beginTransaction().add(R.id.content, mFragment).commit();
        } catch (IllegalStateException e) {
            Logger.logExcpetion(e);
        }
        supportInvalidateOptionsMenu();
    }
    
    private class LoadUserTask extends MyAsyncTask<Void, Void, WeiboUser> {
        String mUserName;
        
        public LoadUserTask(String userName) {
            mUserName = userName;
        }
        
        @Override
        protected WeiboUser doInBackground(Void... v) {
            UserShowDao dao = new UserShowDao();
            dao.setToken(GlobalContext.getCurrentAccount().token);
            dao.setScreenName(mUserName);
            try {
                return dao.execute();
            } catch (WeiboException e) {
                Utilities.notice(e.getMessage());
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(WeiboUser result) {
            if (result != null) {
                mUser = result;
                onUserLoadFinished();
            }
            DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(LOADING_DIALOG);
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }
    
    private class OnFollowFinishedListener implements FollowTask.OnFollowFinishedListener {
        @Override
        public void onFollowFinished(WeiboUser user) {
            mUser = user;
            onUserLoadFinished();
        }
        
        @Override
        public void onFollowFailed(WeiboException e) {
            Utilities.notice(R.string.follow_failed_reason, e.getMessage());
        }
    }
    
    private class OnUnfollowFinishedListener implements UnfollowTask.OnUnfollowFinishedListener {
        @Override
        public void onUnfollowFinished(WeiboUser user) {
            mUser = user;
            onUserLoadFinished();
        }
        
        @Override
        public void onUnfollowFailed(WeiboException e) {
            Utilities.notice(R.string.unfollow_failed_reason, e.getMessage());
        }
    }
}
