package gov.moandor.androidweibo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.activity.UserListActivity;
import gov.moandor.androidweibo.activity.UserWeiboListActivity;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.ImageUtils;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.io.File;
import java.text.DecimalFormat;

public class ProfileFragment extends Fragment {
    public static final String USER = "user";
    
    private static DecimalFormat sDecimalFormat = new DecimalFormat("#,###");
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
    private RefreshTask mRefreshTask;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private WeiboUser mUser;
    private float mFontSize = Utilities.getFontSize();;
    private float mSmallFontSize = mFontSize - 3;
    private float mUserNameFontSize = mFontSize + 5;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        view.findViewById(R.id.weibo_count_layout).setOnClickListener(new OnWeiboCountLayoutClickListener());
        view.findViewById(R.id.following_count_layout).setOnClickListener(new OnFollowingCountLayoutClickListener());
        view.findViewById(R.id.follower_count_layout).setOnClickListener(new OnFollowerCountLayoutClickListener());
        initFontSize();
        buildLayout();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof PullToRefreshAttacherOwner) {
            mPullToRefreshAttacher = ((PullToRefreshAttacherOwner) activity).getAttacher();
        }
    }
    
    public void notifyAccountChanged() {
        if (mRefreshTask != null) {
            mRefreshTask.cancel(true);
        }
        mUser = GlobalContext.getCurrentAccount().user;
        buildLayout();
    }
    
    private void buildLayout() {
        Bundle args = getArguments();
        if (args != null) {
            mUser = args.getParcelable(USER);
        }
        if (mUser == null) {
            mUser = GlobalContext.getCurrentAccount().user;
        }
        AvatarDownloadTask task = new AvatarDownloadTask();
        task.execute();
        mName.setText(mUser.name);
        mSummary.setText(mUser.description);
        mAddress.setText(mUser.location);
        mWeiboCount.setText(sDecimalFormat.format(mUser.statusesCount));
        mFollowingCount.setText(sDecimalFormat.format(mUser.friendsCount));
        mFollowerCount.setText(sDecimalFormat.format(mUser.followersCount));
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
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            return activity.isCurrentFragment(this);
        }
        return false;
    }
    
    public void refresh() {
        if (mRefreshTask != null) {
            return;
        }
        if (isThisCurrentFragment() && mPullToRefreshAttacher != null) {
            mPullToRefreshAttacher.setRefreshing(true);
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
        protected void onPreExecute() {
            mAvatar.setImageDrawable(null);
        }
        
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
            String url = HttpUtils.UrlHelper.USERS_SHOW;
            HttpParams params = new HttpParams();
            params.addParam("access_token", GlobalContext.getCurrentAccount().token);
            params.addParam("uid", String.valueOf(mUser.id));
            try {
                String response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, url, params);
                return Utilities.getWeiboUserFromJson(response);
            } catch (WeiboException e) {
                Utilities.notice(e.getMessage());
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(WeiboUser result) {
            if (mPullToRefreshAttacher != null) {
                mPullToRefreshAttacher.setRefreshComplete();
            }
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
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), UserWeiboListActivity.class);
            intent.putExtra(UserWeiboListActivity.USER, mUser);
            startActivity(intent);
        }
    }
    
    private class OnFollowingCountLayoutClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), UserListActivity.class);
            intent.putExtra(UserListActivity.USER, mUser);
            intent.putExtra(UserListActivity.TYPE, UserListActivity.Type.FOLLOWING);
            startActivity(intent);
        }
    }
    
    private class OnFollowerCountLayoutClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), UserListActivity.class);
            intent.putExtra(UserListActivity.USER, mUser);
            intent.putExtra(UserListActivity.TYPE, UserListActivity.Type.FOLLOWERS);
            startActivity(intent);
        }
    }
}
