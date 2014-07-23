package gov.moandor.androidweibo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.FollowTask;
import gov.moandor.androidweibo.util.UnfollowTask;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class UserDialogFragment extends DialogFragment implements
        FollowTask.OnFollowFinishedListener, UnfollowTask.OnUnfollowFinishedListener {
    public static final String USER = "user";

    private WeiboUser mUser;
    private OnUserChangedListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mUser.name);
        if (mUser.following) {
            builder.setItems(R.array.user_long_click_followed, new OnListClickListener());
        } else {
            builder.setItems(R.array.user_long_click, new OnListClickListener());
        }
        return builder.create();
    }

    @Override
    public void onFollowFinished(WeiboUser user) {
        Utilities.notice(R.string.followed_successfully);
        mUser = user;
        notifyUserChanged();
    }

    @Override
    public void onFollowFailed(WeiboException e) {
        Utilities.notice(R.string.follow_failed_reason, e.getMessage());
    }

    @Override
    public void onUnfollowFinished(WeiboUser user) {
        Utilities.notice(R.string.unfollowed_successfully);
        mUser = user;
        notifyUserChanged();
    }

    @Override
    public void onUnfollowFailed(WeiboException e) {
        Utilities.notice(R.string.unfollow_failed_reason, e.getMessage());
    }

    public void setOnUserChangedListener(OnUserChangedListener l) {
        mListener = l;
    }

    private void notifyUserChanged() {
        if (mListener != null) {
            mListener.onUserChanged(mUser);
        }
    }

    private class OnListClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    if (mUser.following) {
                        new UnfollowTask(mUser, UserDialogFragment.this).execute();
                    } else {
                        new FollowTask(mUser, UserDialogFragment.this).execute();
                    }
                    break;
                case 1:
                    Utilities.ignoreUser(mUser);
                    Utilities.notice(R.string.ignored_successfully);
                    break;
            }
        }
    }

    public static interface OnUserChangedListener {
        public void onUserChanged(WeiboUser user);
    }
}
