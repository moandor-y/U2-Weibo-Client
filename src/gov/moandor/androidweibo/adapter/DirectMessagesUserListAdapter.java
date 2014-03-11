package gov.moandor.androidweibo.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.FriendsUserListAdapter.ViewHolder;
import gov.moandor.androidweibo.bean.DirectMessagesUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

import java.util.List;

public class DirectMessagesUserListAdapter extends AbsBaseAdapter {
    private List<DirectMessagesUser> mUsers;
    private ImageDownloader.ImageType mAvatarType = Utilities.getAvatarType();
    private boolean mNoPictureModeEnabled = GlobalContext.isNoPictureMode();
    private int mSelectedPosition = -1;
    
    @Override
    public int getCount() {
        return mUsers.size();
    }
    
    @Override
    public DirectMessagesUser getItem(int position) {
        return mUsers.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return mUsers.get(position).user.id;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.direct_messages_user_list_item, parent, false);
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
        // TODO Auto-generated method stub
        return null;
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
