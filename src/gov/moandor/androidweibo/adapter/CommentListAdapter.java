package gov.moandor.androidweibo.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public class CommentListAdapter extends AbsTimelineListAdapter<WeiboComment> {
    @Override
    View inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.comment_list_item, parent, false);
    }
    
    @Override
    ViewHolder initViewHolder(View view) {
        CommentListViewHolder holder = new CommentListViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.userName = (TextView) view.findViewById(R.id.user_name);
        holder.time = (TextView) view.findViewById(R.id.time);
        holder.text = (TextView) view.findViewById(R.id.text);
        holder.repliedText = (TextView) view.findViewById(R.id.replied_text);
        return holder;
    }
    
    @Override
    void initLayout(ViewHolder vh) {
        super.initLayout(vh);
        CommentListViewHolder holder = (CommentListViewHolder) vh;
        holder.repliedText.setOnTouchListener(mTextOnTouchListener);
        holder.repliedText.setTextSize(mFontSize);
    }
    
    @Override
    void buildContent(ViewHolder vh, WeiboComment comment, int position) {
        super.buildContent(vh, comment, position);
        CommentListViewHolder holder = (CommentListViewHolder) vh;
        if (!Long.valueOf(comment.id).equals(holder.repliedText.getTag())) {
            if (!TextUtils.isEmpty(comment.repliedTextSpannable)) {
                holder.repliedText.setText(comment.repliedTextSpannable);
            } else {
                String text;
                Resources res = GlobalContext.getInstance().getResources();
                if (comment.repliedComment != null) {
                    text = comment.repliedComment.text;
                    WeiboUser user = comment.repliedComment.weiboUser;
                    if (user != null && !TextUtils.isEmpty(user.name)) {
                        if (user.id == GlobalContext.getCurrentAccount().id) {
                            text = res.getString(R.string.replied_to_my_comment) + " " + text;
                        } else {
                            text = res.getString(R.string.replied_to_comment_by, user.name) + " " + text;
                        }
                    }
                } else {
                    text = comment.weiboStatus.text;
                    WeiboUser user = comment.weiboStatus.weiboUser;
                    if (user != null && !TextUtils.isEmpty(user.name)) {
                        if (user.id == GlobalContext.getCurrentAccount().id) {
                            text = res.getString(R.string.commented_on_my_weibo) + " " + text;
                        } else {
                            text = res.getString(R.string.commented_on_weibo_by, user.name) + " " + text;
                        }
                    }
                }
                comment.repliedTextSpannable = TextUtils.addWeiboLinks(text);
                holder.repliedText.setText(comment.repliedTextSpannable);
            }
        }
    }
    
    private static class CommentListViewHolder extends ViewHolder {
        public TextView repliedText;
    }
}
