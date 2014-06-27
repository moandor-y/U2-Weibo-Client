package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TimeUtils;

import java.text.ParseException;
import java.util.List;

public class DmConversationAdapter extends AbsTimelineListAdapter<DirectMessage> {
    private static final long MIN_TIME_TO_DISPLAY = 1000 * 60 * 10;
    
    private WeiboUser mCurrentUser = GlobalContext.getCurrentAccount().user;
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(getCount() - 1 - position, convertView, parent);
    }
    
    @Override
    public int positionOf(DirectMessage bean) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int positionOf(long id) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void updatePosition(int position, DirectMessage bean) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeItem(int position) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setSelectedPosition(int position) {
        super.setSelectedPosition(getCount() - 1 - position);
    }
    
    @Override
    public int getSelection() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addAllFirst(List<DirectMessage> beans) {
        mBeans.addAll(0, beans);
    }
    
    @Override
    View inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.dm_conversation_item, parent, false);
    }
    
    @Override
    ViewHolder initViewHolder(View view) {
        DmConversationViewHolder holder = new DmConversationViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar_self);
        holder.avatarOppo = (ImageView) view.findViewById(R.id.avatar_oppo);
        holder.text = (TextView) view.findViewById(R.id.text);
        holder.time = (TextView) view.findViewById(R.id.time);
        return holder;
    }
    
    @Override
    void initLayout(ViewHolder holder) {
        holder.time.setTextSize(mTimeFontSize);
        holder.text.setTextSize(mFontSize);
        holder.text.setOnTouchListener(mTextOnTouchListener);
    }
    
    @Override
    void buildUserLayout(ViewHolder h, WeiboUser user, int position) {
        DmConversationViewHolder holder = (DmConversationAdapter.DmConversationViewHolder) h;
        if (user.id == mCurrentUser.id) {
            holder.avatarOppo.setVisibility(View.GONE);
            holder.avatar.setVisibility(View.VISIBLE);
            buildAvatar(holder.avatar, user, getCount() - 1 - position);
        } else {
            holder.avatar.setVisibility(View.GONE);
            holder.avatarOppo.setVisibility(View.VISIBLE);
            buildAvatar(holder.avatarOppo, user, getCount() - 1 - position);
        }
        
    }
    
    @Override
    void buildTime(ViewHolder holder, DirectMessage message, int position) {
        if (position == getCount() - 1 || shouldDisplayTime(message, mBeans.get(position + 1))) {
            holder.time.setVisibility(View.VISIBLE);
            super.buildTime(holder, message, position);
        } else {
            holder.time.setVisibility(View.GONE);
        }
    }
    
    private static boolean shouldDisplayTime(DirectMessage message, DirectMessage prevMessage) {
        try {
            long time = TimeUtils.parseSinaTime(message);
            long prevTime = TimeUtils.parseSinaTime(prevMessage);
            return time - prevTime > MIN_TIME_TO_DISPLAY;
        } catch (ParseException e) {
            Logger.logException(e);
            return false;
        }
    }
    
    private static class DmConversationViewHolder extends ViewHolder {
        public ImageView avatarOppo;
    }
}
