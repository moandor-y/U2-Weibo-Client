package gov.moandor.androidweibo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.fragment.DirectMessagesUserListFragment;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

import java.util.ArrayList;
import java.util.List;

public class DirectMessagesUserListAdapter extends AbsBaseAdapter {
    private List<DirectMessagesUser> mDmUsers = new ArrayList<DirectMessagesUser>();
    private ImageDownloader.ImageType mAvatarType = Utilities.getAvatarType();
    private DirectMessagesUserListFragment mFragment;
    private boolean mNoPictureModeEnabled = GlobalContext.isNoPictureMode();
    private int mSelectedPosition = -1;
    
    public DirectMessagesUserListAdapter(DirectMessagesUserListFragment fragment) {
        mFragment = fragment;
    }
    
    @Override
    public int getCount() {
        return mDmUsers.size();
    }
    
    @Override
    public DirectMessagesUser getItem(int position) {
        return mDmUsers.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return mDmUsers.get(position).user.id;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.dm_user_list_item, parent, false);
            holder = initViewHolder(convertView);
            convertView.setTag(holder);
            holder.userName.setTextSize(mFontSize);
            holder.userName.getPaint().setFakeBoldText(true);
            holder.message.setTextSize(mFontSize);
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
        DirectMessagesUser dmUser = mDmUsers.get(position);
        DirectMessage message = dmUser.message;
        WeiboUser user = dmUser.user;
        holder.userName.setText(user.name);
        if (!mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.VISIBLE);
            ImageDownloader.downloadAvatar(holder.avatar, user, mFragment.isListViewFling(), mAvatarType);
        }
        holder.message.setText(message.text);
        if (position == mSelectedPosition) {
            convertView.setBackgroundResource(R.color.ics_blue_semi);
        } else {
            convertView.setBackgroundResource(0);
        }
        return convertView;
    }
    
    public void updateDataSet(List<DirectMessagesUser> data) {
        mDmUsers.clear();
        mDmUsers.addAll(data);
    }
    
    public void addAll(List<DirectMessagesUser> data) {
        mDmUsers.addAll(data);
    }
    
    private ViewHolder initViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.userName = (TextView) view.findViewById(R.id.user_name);
        holder.message = (TextView) view.findViewById(R.id.message);
        return holder;
    }
    
    private static class ViewHolder {
        public ImageView avatar;
        public TextView userName;
        public TextView message;
    }
}
