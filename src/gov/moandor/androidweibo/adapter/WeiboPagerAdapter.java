package gov.moandor.androidweibo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.text.DecimalFormat;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.WeiboCommentListFragment;
import gov.moandor.androidweibo.fragment.WeiboFragment;
import gov.moandor.androidweibo.fragment.WeiboRepostListFragment;
import gov.moandor.androidweibo.util.GlobalContext;

public class WeiboPagerAdapter extends FragmentPagerAdapter {
    public static final int TAB_COUNT = 3;
    public static final int WEIBO = 0;
    public static final int COMMENT_LIST = 1;
    public static final int REPOST_LIST = 2;
    
    private String[] mTitles = GlobalContext.getInstance().getResources().getStringArray(R.array.weibo_tabs);
    private int mCommentCount;
    private int mRepostCount;
    
    public WeiboPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    
    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case WEIBO:
            return new WeiboFragment();
        case COMMENT_LIST:
            return new WeiboCommentListFragment();
        case REPOST_LIST:
            return new WeiboRepostListFragment();
        }
        return null;
    }
    
    @Override
    public int getCount() {
        return TAB_COUNT;
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        String title = mTitles[position];
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        switch (position) {
        case COMMENT_LIST:
            if (mCommentCount > 0) {
                title += " (" + decimalFormat.format(mCommentCount) + ")";
            }
            break;
        case REPOST_LIST:
            if (mRepostCount > 0) {
                title += " (" + decimalFormat.format(mRepostCount) + ")";
            }
            break;
        }
        return title;
    }
    
    public void setCommentCount(int count) {
        mCommentCount = count;
    }
    
    public void setRepostCount(int count) {
        mRepostCount = count;
    }
}
