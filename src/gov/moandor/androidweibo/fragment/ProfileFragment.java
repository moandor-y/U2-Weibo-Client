package gov.moandor.androidweibo.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.activity.UserListActivity;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.UserShowDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.ImageUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class ProfileFragment extends Fragment {
    public static final String USER = "user";

    private ImageView mAvatar;
    private TextView mName;
    private TextView mSummary;
    private TextView mAddress;
    private TextView mWeiboCount;
    private TextView mFollowingCount;
    private TextView mFollowerCount;
    private TextView mWeiboCountLabel;
    private TextView mFollowingCountLabel;
    private TextView mFollowerCountLabel;
    private TextView mSummaryLabel;
    private TextView mAddressLabel;
    private TextView mStatisticsLabel;
    private View mWeiboCountLayout;
    private View mFollowingCountLayout;
    private View mFollowerCountLayout;
    private RefreshTask mRefreshTask;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WeiboUser mUser;
    private float mFontSize = Utilities.getFontSize();
    ;
    private float mSmallFontSize = mFontSize - 3;
    private float mUserNameFontSize = mFontSize + 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        if (args != null) {
            mUser = args.getParcelable(USER);
        }
        if (mUser == null) {
            mUser = GlobalContext.getCurrentAccount().user;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mName = (TextView) view.findViewById(R.id.name);
        mSummary = (TextView) view.findViewById(R.id.summary);
        mAddress = (TextView) view.findViewById(R.id.address);
        mWeiboCount = (TextView) view.findViewById(R.id.weibo_count);
        mFollowingCount = (TextView) view.findViewById(R.id.following_count);
        mFollowerCount = (TextView) view.findViewById(R.id.follower_count);
        mWeiboCountLabel = (TextView) view.findViewById(R.id.weibo_count_label);
        mFollowingCountLabel = (TextView) view.findViewById(R.id.following_count_label);
        mFollowerCountLabel = (TextView) view.findViewById(R.id.follower_count_label);
        mSummaryLabel = (TextView) view.findViewById(R.id.summary_label);
        mAddressLabel = (TextView) view.findViewById(R.id.address_label);
        mStatisticsLabel = (TextView) view.findViewById(R.id.statistics_label);
        mWeiboCountLayout = view.findViewById(R.id.weibo_count_layout);
        mFollowingCountLayout = view.findViewById(R.id.following_count_layout);
        mFollowerCountLayout = view.findViewById(R.id.follower_count_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new OnListRefreshListener());
        mSwipeRefreshLayout.setColorScheme(R.color.swipe_refresh_color1, R.color.swipe_refresh_color2,
                R.color.swipe_refresh_color3, R.color.swipe_refresh_color4);
        initFontSize();
        buildLayout();
    }

    public void notifyAccountChanged() {
        if (mRefreshTask != null) {
            mRefreshTask.cancel(true);
        }
        mUser = GlobalContext.getCurrentAccount().user;
        buildLayout();
    }

    private void buildLayout() {
        AvatarDownloadTask task = new AvatarDownloadTask();
        task.execute();
        mName.setText(mUser.name);
        mSummary.setText(mUser.description);
        mAddress.setText(mUser.location);
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        mWeiboCount.setText(decimalFormat.format(mUser.statusesCount));
        mFollowingCount.setText(decimalFormat.format(mUser.friendsCount));
        mFollowerCount.setText(decimalFormat.format(mUser.followersCount));
        if (mUser.gender.equals("m")) {
            mName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_male, 0);
        } else if (mUser.gender.equals("f")) {
            mName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_female, 0);
        }
        if (ConfigManager.isBmEnabled() || mUser.id == GlobalContext.getCurrentAccount().user.id) {
            mWeiboCountLayout.setOnClickListener(new OnWeiboCountLayoutClickListener());
            mFollowingCountLayout.setOnClickListener(new OnFollowingCountLayoutClickListener());
            mFollowerCountLayout.setOnClickListener(new OnFollowerCountLayoutClickListener());
        } else {
            mWeiboCountLayout.setClickable(false);
            mFollowingCountLayout.setClickable(false);
            mFollowerCountLayout.setClickable(false);
        }
    }

    private void initFontSize() {
        mName.setTextSize(mUserNameFontSize);
        mSummary.setTextSize(mSmallFontSize);
        mAddress.setTextSize(mSmallFontSize);
        mWeiboCountLabel.setTextSize(mSmallFontSize);
        mFollowingCountLabel.setTextSize(mSmallFontSize);
        mFollowerCountLabel.setTextSize(mSmallFontSize);
        mSummaryLabel.setTextSize(mSmallFontSize);
        mAddressLabel.setTextSize(mSmallFontSize);
        mStatisticsLabel.setTextSize(mSmallFontSize);
        mWeiboCount.setTextSize(mFontSize);
        mFollowingCount.setTextSize(mFontSize);
        mFollowerCount.setTextSize(mFontSize);
    }

    private boolean isThisCurrentFragment() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                return activity.isCurrentFragment(this);
            }
            return false;
        } else {
            return true;
        }
    }

    public void refresh() {
        if (mRefreshTask != null) {
            return;
        }
        if (isThisCurrentFragment()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        mRefreshTask = new RefreshTask();
        mRefreshTask.execute();
    }

    public WeiboUser getUser() {
        return mUser;
    }

    private class AvatarDownloadTask extends MyAsyncTask<Void, Void, Bitmap> {
        private String mUrl;

        @Override
        protected Bitmap doInBackground(Void... params) {
            mUrl = mUser.avatarLargeUrl;
            Bitmap bitmap = GlobalContext.getBitmapCache().get(mUrl);
            if (bitmap != null) {
                return bitmap;
            }
            int width = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.profile_avatar_width);
            int height =
                    GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.profile_avatar_height);
            String path = FileUtils.getAccountAvatarPathFromUrl(mUrl);
            File file = new File(path);
            if (file.exists()) {
                bitmap = ImageUtils.getBitmapFromFile(path, width, height);
                if (bitmap != null) {
                    return bitmap;
                }
            } else {
                path = FileUtils.getImagePathFromUrl(mUrl, ImageDownloader.ImageType.AVATAR_LARGE);
                file = new File(path);
                if (file.exists()) {
                    bitmap = ImageUtils.getBitmapFromFile(path, width, height);
                    if (bitmap != null) {
                        return bitmap;
                    }
                }
            }
            if (ImageUtils.getBitmapFromNetwork(mUrl, path, null)) {
                return ImageUtils.getBitmapFromFile(path, width, height);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mAvatar.setImageDrawable(null);
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                GlobalContext.getBitmapCache().put(mUrl, result);
                mAvatar.setImageBitmap(result);
            }
        }
    }

    private class RefreshTask extends MyAsyncTask<Void, Void, WeiboUser> {
        @Override
        protected WeiboUser doInBackground(Void... v) {
            UserShowDao dao = new UserShowDao();
            dao.setToken(GlobalContext.getCurrentAccount().token);
            dao.setUid(mUser.id);
            try {
                return dao.execute();
            } catch (WeiboException e) {
                Utilities.notice(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(WeiboUser result) {
            mSwipeRefreshLayout.setRefreshing(false);
            mRefreshTask = null;
            if (result == null) {
                return;
            }
            Account account = GlobalContext.getCurrentAccount();
            if (account.user.id == result.id) {
                account.user = result;
                GlobalContext.addOrUpdateAccount(account);
            }
            mUser = result;
            buildLayout();
        }
    }

    private class OnWeiboCountLayoutClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(ActivityUtils.userWeiboListActivity(mUser));
        }
    }

    private class OnFollowingCountLayoutClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(ActivityUtils.userListActivity(mUser, UserListActivity.Type.FOLLOWING));
        }
    }

    private class OnFollowerCountLayoutClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(ActivityUtils.userListActivity(mUser, UserListActivity.Type.FOLLOWERS));
        }
    }

    private class OnListRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }
}
