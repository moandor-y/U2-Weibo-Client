package gov.moandor.androidweibo.adapter;

import android.view.View;
import android.view.ViewGroup;
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
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
