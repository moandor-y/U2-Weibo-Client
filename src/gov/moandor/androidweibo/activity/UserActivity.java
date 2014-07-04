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
import gov.moandor.androidweibo.dao.BaseHttpDao;
import gov.moandor.androidweibo.dao.UserDomainShowDao;
import gov.moandor.androidweibo.dao.UserShowDao;
import gov.moandor.androidweibo.fragment.ProfileFragment;
import gov.moandor.androidweibo.fragment.ProgressDialogFragment;
import gov.moandor.androidweibo.util.FollowTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.UnfollowTask;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class UserActivity extends AbsActivity {
    public static final String USER = Utilities.buildIntentExtraName("USER");
    public static final String USER_NAME = Utilities.buildIntentExtraName("USER_NAME");
    public static final String USER_ID = Utilities.buildIntentExtraName("USER_ID");
    public static final String USER_DOMAIN = Utilities.buildIntentExtraName("USER_DOMAIN");
    private static final String LOADING_DIALOG = "loading_dialog";

    private WeiboUser mUser;
    private ProfileFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getIntent().getParcelableExtra(USER);
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (ProfileFragment) fm.findFragmentById(android.R.id.content);
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
            long userId = getIntent().getLongExtra(USER_ID, 0);
            String userName = getIntent().getStringExtra(USER_NAME);
            String userDomain = getIntent().getStringExtra(USER_DOMAIN);
            LoadUserTask task = new LoadUserTask();
            if (userId != 0) {
                task.setUserId(userId);
            } else if (!TextUtils.isEmpty(userName)) {
                task.setUserName(userName);
            } else if (!TextUtils.isEmpty(userDomain)) {
                task.setUserDomain(userDomain);
            } else {
                Uri data = getIntent().getData();
                userName = data.toString();
                int index = userName.lastIndexOf("@");
                userName = userName.substring(index + 1);
                task.setUserName(userName);
            }
            task.execute();
            DialogFragment dialog = ProgressDialogFragment.newInstance(getString(R.string.loading));
            dialog.show(getSupportFragmentManager(), LOADING_DIALOG);
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
            fm.beginTransaction().replace(android.R.id.content, mFragment).commit();
        } catch (IllegalStateException e) {
            Logger.logException(e);
        }
        supportInvalidateOptionsMenu();
    }

    private class LoadUserTask extends MyAsyncTask<Void, Void, WeiboUser> {
        private long mUserId;
        private String mUserName;
        private String mUserDomain;
        private String mToken;

        private void setUserId(long userId) {
            mUserId = userId;
        }

        private void setUserName(String userName) {
            mUserName = userName;
        }

        private void setUserDomain(String userDomain) {
            mUserDomain = userDomain;
        }

        @Override
        protected void onPreExecute() {
            mToken = GlobalContext.getCurrentAccount().token;
        }

        @Override
        protected WeiboUser doInBackground(Void... v) {
            BaseHttpDao<WeiboUser> dao;
            if (TextUtils.isEmpty(mUserDomain)) {
                UserShowDao userShowDao = new UserShowDao();
                userShowDao.setToken(mToken);
                userShowDao.setUid(mUserId);
                userShowDao.setScreenName(mUserName);
                dao = userShowDao;
            } else {
                UserDomainShowDao userDomainShowDao = new UserDomainShowDao();
                userDomainShowDao.setToken(mToken);
                userDomainShowDao.setDomain(mUserDomain);
                dao = userDomainShowDao;
            }
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
