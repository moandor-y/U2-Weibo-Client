package gov.moandor.androidweibo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsItemBean;
import gov.moandor.androidweibo.util.Utilities;

public class WeiboTimelineListAdapter<T extends AbsItemBean> extends AbsTimelineListAdapter<T> {
    public WeiboTimelineListAdapter() {
        super();
        mNoPictureModeEnabled = mNoPictureModeEnabled || !Utilities.isCommentRepostListAvatarEnabled();
    }
    
    @Override
    View inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.weibo_timeline_list_item, parent, false);
    }
    
    @Override
    ViewHolder initViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.userName = (TextView) view.findViewById(R.id.user_name);
        holder.time = (TextView) view.findViewById(R.id.time);
        holder.text = (TextView) view.findViewById(R.id.text);
        return holder;
    }
}
