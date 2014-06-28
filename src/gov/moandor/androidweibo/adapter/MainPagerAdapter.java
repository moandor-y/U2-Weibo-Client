package gov.moandor.androidweibo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.util.GlobalContext;

public class MainPagerAdapter extends PagerAdapter {
    private int mWeiboUnreadCount;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction;
    private Fragment mCurrentPrimaryItem;
    private Fragment[] mFragments;
    private String[] mTitles = GlobalContext.getInstance().getResources().getStringArray(R.array.main_tabs);

    public MainPagerAdapter(FragmentManager fm, Fragment[] fragments) {
        mFragmentManager = fm;
        mFragments = fragments;
    }

    public static String makeFragmentName(int position) {
        return "MainPagerAdapter:" + ":" + position;
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        String name = makeFragmentName(position);
        Fragment fragment = mFragments[position];
        if (!fragment.isAdded()) {
            mCurTransaction.add(container.getId(), fragment, name);
        } else {
            mCurTransaction.attach(fragment);
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        mCurTransaction.detach((Fragment) object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = mTitles[position];
        if (position == MainActivity.WEIBO_LIST && mWeiboUnreadCount > 0) {
            title += " (" + mWeiboUnreadCount + ")";
        }
        return title;
    }

    public int getWeiboUnreadCount() {
        return mWeiboUnreadCount;
    }

    public void setWeiboUnreadCount(int count) {
        mWeiboUnreadCount = count;
    }
}
