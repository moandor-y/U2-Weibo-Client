package gov.moandor.androidweibo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Arrays;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.MainPagerAdapter;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.UnreadCount;
import gov.moandor.androidweibo.bean.WeiboGroup;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.GroupsDao;
import gov.moandor.androidweibo.fragment.AbsMainTimelineFragment;
import gov.moandor.androidweibo.fragment.AtmeListFragment;
import gov.moandor.androidweibo.fragment.CommentListFragment;
import gov.moandor.androidweibo.fragment.FindUserDialogFragment;
import gov.moandor.androidweibo.fragment.MainDrawerFragment;
import gov.moandor.androidweibo.fragment.ProfileFragment;
import gov.moandor.androidweibo.fragment.WeiboListFragment;
import gov.moandor.androidweibo.notification.FetchUnreadMessageService;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class MainActivity extends AbsActivity implements ViewPager.OnPageChangeListener,
        MainDrawerFragment.OnAccountClickListener, ActionBar.OnNavigationListener {
    public static final int WEIBO_LIST = 0;
    public static final int ATME_LIST = 1;
    public static final int COMMENT_LIST = 2;
    public static final String UNREAD_PAGE_POSITION = Utilities.buildIntentExtraName("UNREAD_PAGE_POSITION");
    public static final String UNREAD_GROUP = Utilities.buildIntentExtraName("UNREAD_GROUP");
    public static final String ACTION_UNREAD_UPDATED = Utilities.buildIntentExtraName("ACTION_UNREAD_UPDATED");
    public static final String UNREAD_COUNT = Utilities.buildIntentExtraName("UNREAD_COUNT");
    public static final String ACTION_ACCOUNT_CHANGED = Utilities.buildIntentExtraName("ACTION_ACCOUNT_CHANGED");
    public static final String NEW_ACCOUNT_INDEX = Utilities.buildIntentExtraName("CHANGED_ACCOUNT_INDEX");
    public static final String OLD_ACCOUNT_INDEX = Utilities.buildIntentExtraName("PREVIOUS_ACCOUNT_INDEX");
    private static final String STATE_TAB = "state_tab";
    private static final String STATE_UNREAD_COUNT = "state_unread_count";
    private static final String STATE_GROUPS = "state_groups";
    private static final String DIALOG_FIND_USER = "dialog_find_user";
    private static final int PROFILE = 3;

    private static boolean sRunning;

    private int mUnreadPage = -1;
    private int mUnreadGroup = -1;
    private ViewPager mViewPager;
    private WeiboListFragment mWeiboListFragment;
    private AtmeListFragment mAtmeListFragment;
    private CommentListFragment mCommentListFragment;
    private ProfileFragment mProfileFragment;
    private DrawerLayout mDrawerLayout;
    private MainDrawerFragment mDrawerFragment;
    private ArrayAdapter<String> mWeiboListSpinnerAdapter;
    private ArrayAdapter<String> mAtmeListSpinnerAdapter;
    private ArrayAdapter<String> mCommentListSpinnerAdapter;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private PagerSlidingTabStrip mTabStrip;
    private MainPagerAdapter mPagerAdapter;
    private WeiboGroup[] mGroups;
    private Object mGroupsLoadLock = new Object();
    private MyAsyncTask<Void, Void, WeiboGroup[]> mLoadWeiboGroupsTask;

    public static boolean isRunning() {
        return sRunning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GlobalContext.getCurrentAccount() == null) {
            startActivity(ActivityUtils.authorizeActivity());
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        mWeiboListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.weibo_list_spinner))));
        mWeiboListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        mAtmeListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        getResources().getStringArray(R.array.atme_list_spinner));
        mAtmeListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        mCommentListSpinnerAdapter =
                new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, android.R.id.text1,
                        getResources().getStringArray(R.array.comment_list_spinner));
        mCommentListSpinnerAdapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.open_drawer,
                        R.string.close_drawer);
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDrawerFragment = (MainDrawerFragment) fragmentManager.findFragmentById(R.id.left_drawer);
        if (mDrawerFragment == null) {
            mDrawerFragment = new MainDrawerFragment();
        }
        if (!mDrawerFragment.isAdded()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.left_drawer, mDrawerFragment);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
        mWeiboListFragment =
                (WeiboListFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(WEIBO_LIST));
        if (mWeiboListFragment == null) {
            mWeiboListFragment = new WeiboListFragment();
        }
        mAtmeListFragment =
                (AtmeListFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(ATME_LIST));
        if (mAtmeListFragment == null) {
            mAtmeListFragment = new AtmeListFragment();
        }
        mCommentListFragment =
                (CommentListFragment) fragmentManager
                        .findFragmentByTag(MainPagerAdapter.makeFragmentName(COMMENT_LIST));
        if (mCommentListFragment == null) {
            mCommentListFragment = new CommentListFragment();
        }
        mProfileFragment =
                (ProfileFragment) fragmentManager.findFragmentByTag(MainPagerAdapter.makeFragmentName(PROFILE));
        if (mProfileFragment == null) {
            mProfileFragment = new ProfileFragment();
        }
        Fragment[] fragments = {mWeiboListFragment, mAtmeListFragment, mCommentListFragment, mProfileFragment};
        mPagerAdapter = new MainPagerAdapter(fragmentManager, fragments);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mUnreadPage = getIntent().getIntExtra(UNREAD_PAGE_POSITION, -1);
        if (savedInstanceState != null) {
            Parcelable[] parcelables = savedInstanceState.getParcelableArray(STATE_GROUPS);
            if (parcelables != null) {
                mGroups = new WeiboGroup[parcelables.length];
                for (int i = 0; i < parcelables.length; i++) {
                    mGroups[i] = (WeiboGroup) parcelables[i];
                }
                setupSpinnerGroups();
            }
            int tab = savedInstanceState.getInt(STATE_TAB);
            mViewPager.setCurrentItem(tab);
            onPageSelected(tab);
            int unreadCount = savedInstanceState.getInt(STATE_UNREAD_COUNT);
            mPagerAdapter.setWeiboUnreadCount(unreadCount);
        } else if (mUnreadPage != -1) {
            int accountIndex = getIntent().getIntExtra(FetchUnreadMessageService.ACCOUNT_INDEX, -1);
            ConfigManager.setCurrentAccountIndex(accountIndex);
            mUnreadGroup = getIntent().getIntExtra(UNREAD_GROUP, -1);
            Bundle args = new Bundle();
            args.putBoolean(AbsMainTimelineFragment.IS_FROM_UNREAD, true);
            switch (mUnreadPage) {
                case ATME_LIST:
                    mAtmeListFragment.setArguments(args);
                    break;
                case COMMENT_LIST:
                    mCommentListFragment.setArguments(args);
                    break;
            }
            getIntent().removeExtra(UNREAD_PAGE_POSITION);
            getIntent().removeExtra(UNREAD_GROUP);
            mViewPager.setCurrentItem(mUnreadPage);
            onPageSelected(mUnreadPage);
        } else {
            onPageSelected(0);
        }
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setOnPageChangeListener(this);
        mTabStrip.setIndicatorColorResource(R.color.holo_blue_light);
        mTabStrip.setTabPaddingLeftRight(getResources().getDimensionPixelSize(R.dimen.tab_padding));
        Utilities.registerReceiver(mUnreadUpdateReciever, new IntentFilter(ACTION_UNREAD_UPDATED));
        Utilities.registerReceiver(mOnAccountChangedReciever, new IntentFilter(ACTION_ACCOUNT_CHANGED));
        sRunning = true;
        if (mGroups == null) {
            mLoadWeiboGroupsTask = new LoadWeiboGroupsTask();
            mLoadWeiboGroupsTask.execute();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, mViewPager.getCurrentItem());
        outState.putInt(STATE_UNREAD_COUNT, mPagerAdapter.getWeiboUnreadCount());
        outState.putParcelableArray(STATE_GROUPS, mGroups);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sRunning = false;
        Utilities.unregisterReceiver(mUnreadUpdateReciever);
        Utilities.unregisterReceiver(mOnAccountChangedReciever);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem orientation = menu.findItem(R.id.orientation);
        switch (ConfigManager.getScreenOrientation()) {
            case ConfigManager.ORIENTATION_USER:
                orientation.setTitle(R.string.lock_orientation);
                if (Utilities.isScreenLandscape()) {
                    orientation.setIcon(R.drawable.ic_lock_orientation_land);
                } else {
                    orientation.setIcon(R.drawable.ic_lock_orientation_port);
                }
                break;
            case ConfigManager.ORIENTATION_LANDSCAPE:
            case ConfigManager.ORIENTATION_PORTRAIT:
                orientation.setTitle(R.string.unlock_orientation);
                orientation.setIcon(R.drawable.ic_unlock_orientation);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActionBarDrawerToggle.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.write_weibo:
                writeWeibo();
                return true;
            case R.id.refresh:
                refresh();
                return true;
            case R.id.settings:
                settings();
                return true;
            case R.id.orientation:
                toggleOrientationLock();
                return true;
            case R.id.find_user:
                new FindUserDialogFragment().show(getSupportFragmentManager(), DIALOG_FIND_USER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void refresh() {
        switch (mViewPager.getCurrentItem()) {
            case WEIBO_LIST:
                mWeiboListFragment.refresh();
                break;
            case ATME_LIST:
                mAtmeListFragment.refresh();
                break;
            case COMMENT_LIST:
                mCommentListFragment.refresh();
                break;
            case PROFILE:
                mProfileFragment.refresh();
                break;
        }
    }

    private void clearSpinnerGroups() {
        for (WeiboGroup group : mGroups) {
            mWeiboListSpinnerAdapter.remove(group.name);
        }
        mWeiboListSpinnerAdapter.remove(getString(R.string.refresh_groups));
    }

    private void setupSpinnerGroups() {
        for (WeiboGroup group : mGroups) {
            mWeiboListSpinnerAdapter.add(group.name);
        }
        mWeiboListSpinnerAdapter.add(getString(R.string.refresh_groups));
    }

    private void saveGroupsToDatabase(final long accountId, final WeiboGroup[] groups) {
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DatabaseUtils.updateWeiboGroups(accountId, groups);
            }
        });
    }

    private void updateWeiboListSpinner() {
        if (mViewPager.getCurrentItem() != WEIBO_LIST) {
            return;
        }
        ActionBar actionBar = getSupportActionBar();
        if (mGroups == null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(R.string.loading);
        } else {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            actionBar.setListNavigationCallbacks(mWeiboListSpinnerAdapter, this);
            long accountId = GlobalContext.getCurrentAccount().user.id;
            int group = ConfigManager.getWeiboGroup(accountId);
            if (group < mWeiboListSpinnerAdapter.getCount()) {
                actionBar.setSelectedNavigationItem(group);
            } else {
                actionBar.setSelectedNavigationItem(0);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar actionBar = getSupportActionBar();
        if (position == mUnreadPage) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setTitle("");
            switch (position) {
                case ATME_LIST:
                    actionBar.setListNavigationCallbacks(mAtmeListSpinnerAdapter, this);
                    actionBar.setSelectedNavigationItem(mUnreadGroup);
                    break;
                case COMMENT_LIST:
                    actionBar.setListNavigationCallbacks(mCommentListSpinnerAdapter, this);
                    actionBar.setSelectedNavigationItem(mUnreadGroup);
                    break;
            }
            mUnreadPage = -1;
            mUnreadGroup = -1;
            return;
        }
        switch (position) {
            case WEIBO_LIST:
                updateWeiboListSpinner();
                break;
            case ATME_LIST:
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                actionBar.setTitle("");
                actionBar.setListNavigationCallbacks(mAtmeListSpinnerAdapter, this);
                actionBar.setSelectedNavigationItem(ConfigManager.getAtmeFilter());
                break;
            case COMMENT_LIST:
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                actionBar.setTitle("");
                actionBar.setListNavigationCallbacks(mCommentListSpinnerAdapter, this);
                actionBar.setSelectedNavigationItem(ConfigManager.getCommentFilter());
                break;
            case PROFILE:
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setTitle(GlobalContext.getCurrentAccount().user.name);
                break;
        }
    }

    @Override
    public void onAccountClick(int oldPosition, int newPosition) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        if (oldPosition >= 0) {
            Account oldAccount = GlobalContext.getAccount(oldPosition);
            mWeiboListFragment.saveListPosition(oldAccount);
            mAtmeListFragment.saveListPosition(oldAccount);
            mCommentListFragment.saveListPosition(oldAccount);
        }
        clearSpinnerGroups();
        ConfigManager.setCurrentAccountIndex(newPosition);
        mGroups = null;
        updateWeiboListSpinner();
        if (mLoadWeiboGroupsTask != null) {
            mLoadWeiboGroupsTask.cancel(true);
        }
        mLoadWeiboGroupsTask = new LoadWeiboGroupsTask();
        mLoadWeiboGroupsTask.execute();
        mWeiboListFragment.notifyAccountOrGroupChanged();
        mAtmeListFragment.notifyAccountOrGroupChanged();
        mCommentListFragment.notifyAccountOrGroupChanged();
        mProfileFragment.notifyAccountChanged();
        if (mViewPager.getCurrentItem() == PROFILE) {
            getSupportActionBar().setTitle(GlobalContext.getCurrentAccount().user.name);
        }
        mDrawerFragment.notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        switch (mViewPager.getCurrentItem()) {
            case WEIBO_LIST:
                if (itemPosition == mWeiboListSpinnerAdapter.getCount() - 1) {
                    clearSpinnerGroups();
                    mGroups = null;
                    updateWeiboListSpinner();
                    if (mLoadWeiboGroupsTask != null) {
                        mLoadWeiboGroupsTask.cancel(true);
                    }
                    mLoadWeiboGroupsTask = new RefreshGroupsTask();
                    mLoadWeiboGroupsTask.execute();
                    break;
                }
                long accountId = GlobalContext.getCurrentAccount().user.id;
                if (itemPosition != ConfigManager.getWeiboGroup(accountId)) {
                    mWeiboListFragment.saveListPosition(GlobalContext.getCurrentAccount());
                    ConfigManager.setWeiboGroup(itemPosition, accountId);
                    mWeiboListFragment.notifyAccountOrGroupChanged();
                }
                break;
            case ATME_LIST:
                if (itemPosition != ConfigManager.getAtmeFilter()) {
                    mAtmeListFragment.saveListPosition(GlobalContext.getCurrentAccount());
                    ConfigManager.setAtmeFilter(itemPosition);
                    mAtmeListFragment.notifyAccountOrGroupChanged();
                }
                break;
            case COMMENT_LIST:
                if (itemPosition != ConfigManager.getCommentFilter()) {
                    mCommentListFragment.saveListPosition(GlobalContext.getCurrentAccount());
                    ConfigManager.setCommentFilter(itemPosition);
                    mCommentListFragment.notifyAccountOrGroupChanged();
                }
                break;
        }
        return true;
    }

    public boolean isCurrentFragment(Fragment fragment) {
        switch (mViewPager.getCurrentItem()) {
            case WEIBO_LIST:
                return fragment instanceof WeiboListFragment;
            case ATME_LIST:
                return fragment instanceof AtmeListFragment;
            case COMMENT_LIST:
                return fragment instanceof CommentListFragment;
            case PROFILE:
                return fragment instanceof ProfileFragment;
            default:
                return false;
        }
    }

    private void writeWeibo() {
        startActivity(ActivityUtils.writeWeiboActivity());
    }

    private void settings() {
        startActivity(ActivityUtils.settingsActivity());
    }

    private void toggleOrientationLock() {
        if (ConfigManager.getScreenOrientation() == ConfigManager.ORIENTATION_USER) {
            if (Utilities.isScreenLandscape()) {
                ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_LANDSCAPE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_PORTRAIT);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            ConfigManager.setScreenOrientation(ConfigManager.ORIENTATION_USER);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
        supportInvalidateOptionsMenu();
    }

    public void resetWeiboUnreadCount() {
        mPagerAdapter.setWeiboUnreadCount(0);
        mTabStrip.notifyDataSetChanged();
    }

    public void waitForGroupsLoad() {
        synchronized (mGroupsLoadLock) {
            while (mGroups == null) {
                try {
                    mGroupsLoadLock.wait();
                } catch (InterruptedException e) {
                    Logger.logException(e);
                }
            }
        }
    }

    public long getGroupId(int group) {
        return mGroups[group - getResources().getStringArray(R.array.weibo_list_spinner).length].id;
    }

    private class LoadWeiboGroupsTask extends MyAsyncTask<Void, Void, WeiboGroup[]> {
        private long mAccountId;
        private String mToken;

        @Override
        protected void onPreExecute() {
            mAccountId = GlobalContext.getCurrentAccount().user.id;
            mToken = GlobalContext.getCurrentAccount().token;
        }

        @Override
        protected WeiboGroup[] doInBackground(Void... params) {
            WeiboGroup[] groups = DatabaseUtils.getWeiboGroups(mAccountId);
            if (groups == null) {
                GroupsDao dao = new GroupsDao();
                dao.setToken(mToken);
                try {
                    return dao.execute().toArray(new WeiboGroup[0]);
                } catch (WeiboException e) {
                    Logger.logException(e);
                }
            } else {
                return groups;
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeiboGroup[] result) {
            if (result != null) {
                mGroups = result;
                saveGroupsToDatabase(mAccountId, result);
            } else {
                mGroups = new WeiboGroup[0];
            }
            setupSpinnerGroups();
            updateWeiboListSpinner();
            synchronized (mGroupsLoadLock) {
                mGroupsLoadLock.notifyAll();
            }
            mLoadWeiboGroupsTask = null;
        }
    }

    private class RefreshGroupsTask extends MyAsyncTask<Void, Void, WeiboGroup[]> {
        private long mAccountId;
        private String mToken;

        @Override
        protected void onPreExecute() {
            mAccountId = GlobalContext.getCurrentAccount().user.id;
            mToken = GlobalContext.getCurrentAccount().token;
        }

        @Override
        protected WeiboGroup[] doInBackground(Void... params) {
            GroupsDao dao = new GroupsDao();
            dao.setToken(mToken);
            try {
                return dao.execute().toArray(new WeiboGroup[0]);
            } catch (WeiboException e) {
                Logger.logException(e);
                Utilities.notice(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeiboGroup[] result) {
            if (result != null) {
                mGroups = result;
            } else {
                mGroups = new WeiboGroup[0];
            }
            setupSpinnerGroups();
            updateWeiboListSpinner();
            synchronized (mGroupsLoadLock) {
                mGroupsLoadLock.notifyAll();
            }
            mLoadWeiboGroupsTask = null;
            saveGroupsToDatabase(mAccountId, result);
            getSupportActionBar().setSelectedNavigationItem(0);
        }
    }

    private BroadcastReceiver mOnAccountChangedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newIndex = intent.getIntExtra(NEW_ACCOUNT_INDEX, -1);
            int oldIndex = intent.getIntExtra(OLD_ACCOUNT_INDEX, -1);
            if (newIndex != -1 && newIndex != oldIndex) {
                onAccountClick(oldIndex, newIndex);
            }
        }
    };

    private BroadcastReceiver mUnreadUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UnreadCount count = intent.getParcelableExtra(UNREAD_COUNT);
            if (count.weiboStatus > 0) {
                mPagerAdapter.setWeiboUnreadCount(count.weiboStatus);
            }
            mTabStrip.notifyDataSetChanged();
        }
    };
}
