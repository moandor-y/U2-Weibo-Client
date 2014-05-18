package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.WeiboPagerAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.WeiboStatusDao;
import gov.moandor.androidweibo.fragment.WeiboCommentListFragment;
import gov.moandor.androidweibo.fragment.WeiboFragment;
import gov.moandor.androidweibo.fragment.WeiboRepostListFragment;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.FavoriteTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.UnfavoriteTask;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class WeiboActivity extends AbsSwipeBackActivity implements ViewPager.OnPageChangeListener {
    public static final String WEIBO_STATUS;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        WEIBO_STATUS = packageName + ".weibo.status";
    }
    
    private WeiboStatus mWeiboStatus;
    private ViewPager mViewPager;
    private WeiboCommentListFragment mWeiboCommentListFragment;
    private WeiboRepostListFragment mWeiboRepostListFragment;
    private WeiboPagerAdapter mPagerAdapter;
    private PagerSlidingTabStrip mTabStrip;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        Intent intent = getIntent();
        mWeiboStatus = intent.getParcelableExtra(WEIBO_STATUS);
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
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.weibo);
        if (GlobalContext.isInWifi()) {
            new RefreshWeiboInfoTask().execute();
        }
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
        if (mViewPager.getCurrentItem() == WeiboPagerAdapter.WEIBO) {
            menu.findItem(R.id.refresh).setVisible(false);
        } else {
            menu.findItem(R.id.refresh).setVisible(true);
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
            return true;
        case R.id.refresh:
            refresh();
            return true;
        case R.id.favorite:
            new FavoriteTask(mWeiboStatus, new OnFavoriteFinishedListener()).execute();
            return true;
        case R.id.unfavorite:
            new UnfavoriteTask(mWeiboStatus, new OnUnfavoriteFinishedListener()).execute();
            return true;
        case R.id.copy:
            Utilities.copyText(mWeiboStatus.text);
            return true;
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
        supportInvalidateOptionsMenu();
        switch (position) {
        case WeiboPagerAdapter.COMMENT_LIST:
            mWeiboCommentListFragment.onShown();
            break;
        case WeiboPagerAdapter.REPOST_LIST:
            mWeiboRepostListFragment.onShown();
            break;
        }
    }
    
    private void refresh() {
        switch (mViewPager.getCurrentItem()) {
        case WeiboPagerAdapter.COMMENT_LIST:
            mWeiboCommentListFragment.refresh();
            break;
        case WeiboPagerAdapter.REPOST_LIST:
            mWeiboRepostListFragment.refresh();
            break;
        }
    }
    
    private void comment() {
        startActivity(ActivityUtils.writeCommentActivity(mWeiboStatus));
    }
    
    private void repost() {
        startActivity(ActivityUtils.writeWeiboActivity(mWeiboStatus));
    }
    
    private void setResult() {
        Intent data = new Intent();
        data.putExtra(WEIBO_STATUS, mWeiboStatus);
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
            WeiboStatusDao dao = new WeiboStatusDao();
            dao.setToken(GlobalContext.getCurrentAccount().token);
            dao.setId(mWeiboStatus.id);
            try {
                WeiboStatus status = dao.execute();
                return new Integer[]{status.commentCount, status.repostCount};
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Integer[] result) {
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
    
    public static class Translucent extends WeiboActivity {}
}
