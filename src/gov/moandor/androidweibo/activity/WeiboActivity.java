package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.WeiboPagerAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.fragment.WeiboCommentListFragment;
import gov.moandor.androidweibo.fragment.WeiboFragment;
import gov.moandor.androidweibo.fragment.WeiboRepostListFragment;
import gov.moandor.androidweibo.util.FavoriteTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;
import gov.moandor.androidweibo.util.UnfavoriteTask;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class WeiboActivity extends AbsSwipeBackActivity implements ViewPager.OnPageChangeListener,
        PullToRefreshAttacherOwner {
    public static final String WEIBO_STATUS;
    public static final String POSITION;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        WEIBO_STATUS = packageName + ".weibo.status";
        POSITION = packageName + ".position";
    }
    
    private WeiboStatus mWeiboStatus;
    private ViewPager mViewPager;
    private WeiboCommentListFragment mWeiboCommentListFragment;
    private WeiboRepostListFragment mWeiboRepostListFragment;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private WeiboPagerAdapter mPagerAdapter;
    private PagerSlidingTabStrip mTabStrip;
    private RefreshWeiboInfoTask mRefreshTask;
    private int mPosition;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        Intent intent = getIntent();
        mWeiboStatus = intent.getParcelableExtra(WEIBO_STATUS);
        mPosition = intent.getIntExtra(POSITION, -1);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new WeiboPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.setCommentCount(mWeiboStatus.commentCount);
        mPagerAdapter.setRepostCount(mWeiboStatus.repostCount);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(WeiboPagerAdapter.TAB_COUNT - 1);
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mTabStrip.setViewPager(mViewPager);
        mTabStrip.setOnPageChangeListener(this);
        mTabStrip.setDividerColorResource(R.color.tab_divider);
        mTabStrip.setTextColorResource(android.R.color.white);
        mTabStrip.setIndicatorColorResource(R.color.holo_blue_light);
        mTabStrip.setTabPaddingLeftRight(getResources().getDimensionPixelSize(R.dimen.tab_padding));
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.weibo);
        mRefreshTask = new RefreshWeiboInfoTask();
        mRefreshTask.execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_weibo, menu);
        MenuItem shareItem = menu.findItem(R.id.share);
        Utilities.registerShareActionMenu(shareItem, mWeiboStatus);
        return true;
    }
    
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mWeiboStatus.favorited) {
            menu.findItem(R.id.favorite).setVisible(false);
        } else {
            menu.findItem(R.id.unfavorite).setVisible(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.comment:
            comment();
            return true;
        case R.id.repost:
            repost();
        case R.id.refresh:
            refresh();
            return true;
        case R.id.favorite:
            new FavoriteTask(mWeiboStatus, new OnFavoriteFinishedListener()).execute();
            return true;
        case R.id.unfavorite:
            new UnfavoriteTask(mWeiboStatus, new OnUnfavoriteFinishedListener()).execute();
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPageScrollStateChanged(int state) {}
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    
    @Override
    public void onPageSelected(int position) {
        mPullToRefreshAttacher.setRefreshComplete();
        switch (position) {
        case WeiboPagerAdapter.COMMENT_LIST:
            mWeiboCommentListFragment.onShown();
            break;
        case WeiboPagerAdapter.REPOST_LIST:
            mWeiboRepostListFragment.onShown();
            break;
        }
    }
    
    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }
    
    private void refresh() {
        switch (mViewPager.getCurrentItem()) {
        case WeiboPagerAdapter.WEIBO:
            if (mRefreshTask == null && mPullToRefreshAttacher.isEnabled()) {
                mRefreshTask = new RefreshWeiboInfoTask();
                mRefreshTask.execute();
                mPullToRefreshAttacher.setRefreshing(true);
            }
            break;
        case WeiboPagerAdapter.COMMENT_LIST:
            mWeiboCommentListFragment.refresh();
            break;
        case WeiboPagerAdapter.REPOST_LIST:
            mWeiboRepostListFragment.refresh();
            break;
        }
    }
    
    private void comment() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteCommentActivity.class);
        intent.putExtra(WriteCommentActivity.COMMENTED_WEIBO_STATUS, mWeiboStatus);
        startActivity(intent);
    }
    
    private void repost() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteWeiboActivity.class);
        intent.putExtra(WriteWeiboActivity.RETWEET_WEIBO_STATUS, mWeiboStatus);
        startActivity(intent);
    }
    
    private void setResult() {
        Intent data = new Intent();
        data.putExtra(WEIBO_STATUS, mWeiboStatus);
        data.putExtra(POSITION, mPosition);
        setResult(RESULT_OK, data);
    }
    
    public WeiboStatus getWeiboStatus() {
        return mWeiboStatus;
    }
    
    public boolean isCurrentFragment(Fragment fragment) {
        switch (mViewPager.getCurrentItem()) {
        case WeiboPagerAdapter.WEIBO:
            return fragment instanceof WeiboFragment;
        case WeiboPagerAdapter.COMMENT_LIST:
            return fragment instanceof WeiboCommentListFragment;
        case WeiboPagerAdapter.REPOST_LIST:
            return fragment instanceof WeiboRepostListFragment;
        default:
            return false;
        }
    }
    
    public void setWeiboCommentListFragment(WeiboCommentListFragment fragment) {
        mWeiboCommentListFragment = fragment;
    }
    
    public void setWeiboRepostListFragment(WeiboRepostListFragment fragment) {
        mWeiboRepostListFragment = fragment;
    }
    
    private class RefreshWeiboInfoTask extends MyAsyncTask<Void, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(Void... v) {
            HttpParams params = new HttpParams();
            params.putParam("access_token", GlobalContext.getCurrentAccount().token);
            params.putParam("id", mWeiboStatus.id);
            try {
                String response =
                        HttpUtils.executeNormalTask(HttpUtils.Method.GET, HttpUtils.UrlHelper.STATUSES_SHOW, params);
                JSONObject json = new JSONObject(response);
                WeiboStatus status = JsonUtils.getWeiboStatusFromJson(json);
                return new Integer[]{status.commentCount, status.repostCount};
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
            } catch (JSONException e) {
                Logger.logExcpetion(e);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Integer[] result) {
            mRefreshTask = null;
            mPullToRefreshAttacher.setRefreshComplete();
            if (result != null) {
                int commentCount = result[0];
                int repostCount = result[1];
                mPagerAdapter.setCommentCount(commentCount);
                mPagerAdapter.setRepostCount(repostCount);
                mWeiboStatus.commentCount = commentCount;
                mWeiboStatus.repostCount = repostCount;
                mTabStrip.notifyDataSetChanged();
                setResult();
            }
        }
    }
    
    private class OnFavoriteFinishedListener implements FavoriteTask.OnFavoriteFinishedListener {
        @Override
        public void onFavoriteFinished(final WeiboStatus status) {
            mWeiboStatus = status;
            Utilities.notice(R.string.favorited_successfully);
            setResult();
            supportInvalidateOptionsMenu();
        }
        
        @Override
        public void onFavoriteFailed(WeiboException e) {
            Utilities.notice(R.string.favorite_failed_reason, e.getMessage());
        }
    }
    
    private class OnUnfavoriteFinishedListener implements UnfavoriteTask.OnUnfavoriteFinishedListener {
        @Override
        public void onUnfavoriteFinished(final WeiboStatus status) {
            mWeiboStatus = status;
            Utilities.notice(R.string.unfavorited_successfully);
            setResult();
            supportInvalidateOptionsMenu();
        }
        
        @Override
        public void onUnfavoriteFailed(WeiboException e) {
            Utilities.notice(R.string.unfavorite_failed_reason, e.getMessage());
        }
    }
}
