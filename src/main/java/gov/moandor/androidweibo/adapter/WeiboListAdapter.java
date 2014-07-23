package gov.moandor.androidweibo.adapter;

import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.widget.TimelinePicImageView;

public class WeiboListAdapter extends AbsTimelineListAdapter<WeiboStatus> {
    private OnMultiPictureClickListener mOnMultiPictureClickListener;
    private OnPictureClickListener mOnPictureClickListener;
    private ImageDownloader.ImageType mPictureType = Utilities.getListPictureType();

    private float mCountFontSize = mFontSize - 5;

    @Override
    void buildContent(ViewHolder vh, WeiboStatus status, int position) {
        super.buildContent(vh, status, position);
        WeiboListViewHolder holder = (WeiboListViewHolder) vh;
        boolean checkRepostsCount = status.repostCount != 0;
        boolean checkCommentsCount = status.commentCount != 0;
        boolean checkHasPicture =
                mNoPictureModeEnabled
                        && (status.thumbnailPic != null && status.thumbnailPic.length > 0 || status.retweetStatus != null
                        && status.retweetStatus.thumbnailPic != null
                        && status.retweetStatus.thumbnailPic.length > 0);
        boolean chechGps = status.weiboGeo != null;
        if (!checkRepostsCount && !checkCommentsCount && !checkHasPicture && !chechGps) {
            holder.countLayout.setVisibility(View.INVISIBLE);
        } else {
            holder.countLayout.setVisibility(View.VISIBLE);
            if (checkRepostsCount) {
                holder.repostCount.setText(String.valueOf(status.repostCount));
                holder.repostCount.setVisibility(View.VISIBLE);
            } else {
                holder.repostCount.setVisibility(View.GONE);
            }
            if (checkCommentsCount) {
                holder.commentCount.setText(String.valueOf(status.commentCount));
                holder.commentCount.setVisibility(View.VISIBLE);
            } else {
                holder.commentCount.setVisibility(View.GONE);
            }
            if (checkHasPicture) {
                holder.picIcon.setVisibility(View.VISIBLE);
            } else {
                holder.picIcon.setVisibility(View.GONE);
            }
            if (chechGps) {
                holder.gpsIcon.setVisibility(View.VISIBLE);
            } else {
                holder.gpsIcon.setVisibility(View.GONE);
            }
        }
        if (status.thumbnailPic != null && status.picCount > 0) {
            holder.retweet.setVisibility(View.GONE);
            if (!mNoPictureModeEnabled) {
                if (status.picCount == 1) {
                    holder.multiPic.setVisibility(View.GONE);
                    holder.pic.setVisibility(View.VISIBLE);
                    buildPicture(status, holder.pic, position);
                } else {
                    holder.multiPic.setVisibility(View.VISIBLE);
                    holder.pic.setVisibility(View.GONE);
                    buildMultiPicture(status, holder.multiPic, position);
                }
            } else {
                holder.multiPic.setVisibility(View.GONE);
                holder.pic.setVisibility(View.GONE);
            }
        } else {
            holder.multiPic.setVisibility(View.GONE);
            holder.pic.setVisibility(View.GONE);
            if (status.retweetStatus != null) {
                holder.retweet.setVisibility(View.VISIBLE);
                buildRetweetContent(status.retweetStatus, holder, position);
            } else {
                holder.retweet.setVisibility(View.GONE);
            }
        }
    }

    @Override
    void initLayout(ViewHolder vh) {
        super.initLayout(vh);
        WeiboListViewHolder holder = (WeiboListViewHolder) vh;
        holder.retweetText.setOnTouchListener(mTextOnTouchListener);
        holder.retweetText.setTextSize(mFontSize);
        holder.repostCount.setTextSize(mCountFontSize);
        holder.commentCount.setTextSize(mCountFontSize);
    }

    @Override
    public void updateState() {
        super.updateState();
        mPictureType = Utilities.getListPictureType();
    }

    @Override
    View inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.weibo_list_item, parent, false);
    }

    @Override
    ViewHolder initViewHolder(View view) {
        WeiboListViewHolder holder = new WeiboListViewHolder();
        holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.userName = (TextView) view.findViewById(R.id.user_name);
        holder.time = (TextView) view.findViewById(R.id.time);
        holder.text = (TextView) view.findViewById(R.id.text);
        holder.pic = (TimelinePicImageView) view.findViewById(R.id.pic);
        holder.multiPic = (GridLayout) view.findViewById(R.id.pic_multi);
        holder.retweet = (RelativeLayout) view.findViewById(R.id.retweet);
        holder.retweetText = (TextView) view.findViewById(R.id.retweet_text);
        holder.retweetPic = (TimelinePicImageView) view.findViewById(R.id.retweet_pic);
        holder.retweetMultiPic = (GridLayout) view.findViewById(R.id.retweet_pic_multi);
        holder.repostCount = (TextView) view.findViewById(R.id.repost_count);
        holder.commentCount = (TextView) view.findViewById(R.id.comment_count);
        holder.gpsIcon = (ImageView) view.findViewById(R.id.ic_gps);
        holder.picIcon = (ImageView) view.findViewById(R.id.ic_pic);
        holder.countLayout = (LinearLayout) view.findViewById(R.id.count_layout);
        return holder;
    }

    private void buildMultiPicture(WeiboStatus status, GridLayout grid, final int position) {
        for (int i = 0; i < status.picCount; i++) {
            ImageView view = (ImageView) grid.getChildAt(i);
            view.setVisibility(View.VISIBLE);
            ImageDownloader.downloadMultiPicture(view, status, mFragment.isListViewFling(), mPictureType, i);
            final int picIndex = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMultiPictureClickListener != null) {
                        mOnMultiPictureClickListener.onMultiPictureClick(position, picIndex);
                    }
                }
            });
        }
        if (status.picCount < 9) {
            ImageView view;
            switch (status.picCount) {
                case 8:
                    view = (ImageView) grid.getChildAt(8);
                    view.setVisibility(View.INVISIBLE);
                    break;
                case 7:
                    for (int i = 8; i > 6; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 6:
                    for (int i = 8; i > 5; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.GONE);
                    }
                    break;
                case 5:
                    for (int i = 8; i > 5; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.GONE);
                    }
                    view = (ImageView) grid.getChildAt(5);
                    view.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    for (int i = 8; i > 5; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.GONE);
                    }
                    for (int i = 5; i > 3; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.INVISIBLE);
                    }
                    break;
                case 3:
                    for (int i = 8; i > 2; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    for (int i = 8; i > 2; i--) {
                        view = (ImageView) grid.getChildAt(i);
                        view.setVisibility(View.GONE);
                    }
                    view = (ImageView) grid.getChildAt(2);
                    view.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    private void buildPicture(WeiboStatus status, TimelinePicImageView view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPictureClickListener != null) {
                    mOnPictureClickListener.onPictureClick(position);
                }
            }
        });
        ImageDownloader.downloadTimelinePicture(view, status, mFragment.isListViewFling(), mPictureType);
    }

    private void buildRetweetContent(WeiboStatus retweetedStatus, WeiboListViewHolder holder,
                                     int position) {
        if (!Long.valueOf(retweetedStatus.id).equals(holder.retweet.getTag())) {
            if (TextUtils.isEmpty(retweetedStatus.textSpannable)) {
                String text = retweetedStatus.text;
                WeiboUser user = retweetedStatus.weiboUser;
                if (user != null && !TextUtils.isEmpty(user.name)) {
                    text = "@" + user.name + " : " + text;
                }
                retweetedStatus.textSpannable = TextUtils.addWeiboLinks(text);
            }
            holder.retweetText.setText(retweetedStatus.textSpannable);
            holder.retweet.setTag(retweetedStatus.id);
        }
        if (retweetedStatus.thumbnailPic != null && retweetedStatus.picCount > 0 && !mNoPictureModeEnabled) {
            if (retweetedStatus.picCount == 1) {
                holder.retweetMultiPic.setVisibility(View.GONE);
                holder.retweetPic.setVisibility(View.VISIBLE);
                buildPicture(retweetedStatus, holder.retweetPic, position);
            } else {
                holder.retweetMultiPic.setVisibility(View.VISIBLE);
                holder.retweetPic.setVisibility(View.GONE);
                buildMultiPicture(retweetedStatus, holder.retweetMultiPic, position);
            }
        } else {
            holder.retweetPic.setVisibility(View.GONE);
            holder.retweetMultiPic.setVisibility(View.GONE);
        }
    }

    public void setOnMultiPictureClickListener(OnMultiPictureClickListener l) {
        mOnMultiPictureClickListener = l;
    }

    public void setOnPictureClickListener(OnPictureClickListener l) {
        mOnPictureClickListener = l;
    }

    public static interface OnMultiPictureClickListener {
        public void onMultiPictureClick(int position, int picIndex);
    }

    public static interface OnPictureClickListener {
        public void onPictureClick(int position);
    }

    private static class WeiboListViewHolder extends ViewHolder {
        public TimelinePicImageView pic;
        public GridLayout multiPic;
        public RelativeLayout retweet;
        public TextView retweetText;
        public TimelinePicImageView retweetPic;
        public GridLayout retweetMultiPic;
        public TextView repostCount;
        public TextView commentCount;
        public ImageView gpsIcon;
        public ImageView picIcon;
        public LinearLayout countLayout;
    }
}
