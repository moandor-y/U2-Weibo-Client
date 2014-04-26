package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.DirectMessage;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TimeUtils;

import java.text.ParseException;

public class DmConversationAdapter extends AbsTimelineListAdapter<DirectMessage> {
    private static final long MIN_TIME_TO_DISPLAY = 1000 * 60 * 60 * 10;
    
    @Override
    View inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.dm_conversation_item, parent, false);
    }
    
    @Override
    ViewHolder initViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.text = (TextView) view.findViewById(R.id.text);
        holder.time = (TextView) view.findViewById(R.id.time);
        return holder;
    }
    
    @Override
    void initLayout(ViewHolder holder) {
        holder.time.setTextSize(mTimeFontSize);
        holder.text.setTextSize(mFontSize);
        holder.text.setOnTouchListener(mTextOnTouchListener);
        if (mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.INVISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.avatar.getLayoutParams();
            params.width = 0;
            params.rightMargin = 0;
        }
    }
    
    @Override
    void buildUserLayout(ViewHolder holder, WeiboUser user, int position) {
        if (!mNoPictureModeEnabled) {
            holder.avatar.setVisibility(View.VISIBLE);
            buildAvatar(holder.avatar, user, position);
        }
    }
    
    @Override
    void buildTime(ViewHolder holder, DirectMessage message, int position) {
        if (position == 0 || shouldDisplayTime(message, mBeans.get(position - 1))) {
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
            Logger.logExcpetion(e);
            return false;
        }
    }
}
