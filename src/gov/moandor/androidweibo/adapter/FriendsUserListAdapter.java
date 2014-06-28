package gov.moandor.androidweibo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.fragment.AbsUserListFragment;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.Utilities;

public class FriendsUserListAdapter extends AbsBaseAdapter implements ISelectableAdapter<WeiboUser> {
    private List<WeiboUser> mUsers = new ArrayList<WeiboUser>();
    private AbsUserListFragment<FriendsUserListAdapter, WeiboUser> mFragment;
    private ImageDownloader.ImageType mAvatarType = Utilities.getAvatarType();
    private float mFontSizeSmall = mFontSize - 3;
    private boolean mNoPictureModeEnabled = ConfigManager.isNoPictureMode();
    private int mSelectedPosition = -1;

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public WeiboUser getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mUsers.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list_item, parent, false);
            holder = initViewHolder(convertView);
            convertView.setTag(holder);
            holder.userName.setTextSize(mFontSize);
            holder.userName.getPaint().setFakeBoldText(true);
            holder.description.setTextSize(mFontSizeSmall);
            if (mNoPictureModeEnabled) {
                holder.avatar.setVisibility(View.INVISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.avatar.getLayoutParams();
                params.width = 0;
                params.height = 0;
                params.rightMargin = 0;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        WeiboUser user = mUsers.get(position);
        holder.userName.setText(user.name);
        if (!mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.VISIBLE);
            ImageDownloader.downloadAvatar(holder.avatar, user, mFragment.isListViewFling(), mAvatarType);
        }
        holder.description.setText(user.description);
        if (position == mSelectedPosition) {
            convertView.setBackgroundResource(R.color.ics_blue_semi);
        } else {
            convertView.setBackgroundResource(0);
        }
        return convertView;
    }

    public void setFragment(AbsUserListFragment<FriendsUserListAdapter, WeiboUser> fragment) {
        mFragment = fragment;
    }

    @Override
    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    @Override
    public WeiboUser getSelectedItem() {
        return getItem(mSelectedPosition);
    }

    public void updateDataSet(List<WeiboUser> users) {
        mUsers.clear();
        mUsers.addAll(users);
    }

    public void addAll(List<WeiboUser> users) {
        mUsers.addAll(users);
    }

    public void updatePosition(int position, WeiboUser user) {
        mUsers.set(position, user);
    }

    @Override
    public int getSelection() {
        return mSelectedPosition;
    }

    private ViewHolder initViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.userName = (TextView) view.findViewById(R.id.user_name);
        holder.description = (TextView) view.findViewById(R.id.description);
        return holder;
    }

    private static class ViewHolder {
        public ImageView avatar;
        public TextView userName;
        public TextView description;
    }
}
