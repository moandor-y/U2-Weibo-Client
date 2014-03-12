package gov.moandor.androidweibo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.ImageViewerActivity;
import gov.moandor.androidweibo.activity.UserActivity;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.WeiboDetailPictureReadTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.LongClickableLinkMovementMethod;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.TimeUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboTextUrlSpan;
import gov.moandor.androidweibo.widget.WeiboDetailPicView;

public class WeiboFragment extends Fragment {
    private WeiboStatus mWeiboStatus;
    private ImageDownloader.ImageType mAvatarType = Utilities.getAvatarType();
    private ImageDownloader.ImageType mPictureType = Utilities.getDetailPictureType();
    private ImageView mAvatar;
    private WeiboDetailPicView mPicture;
    private WeiboDetailPicView mRetweetPicture;
    private GridLayout mPictureMulti;
    private GridLayout mRetweetPictureMulti;
    private RelativeLayout mRetweetLayout;
    private TextView mUserName;
    private TextView mTime;
    private TextView mSource;
    private TextView mText;
    private TextView mRetweetText;
    private float mFontSize = Utilities.getFontSize();;
    private float mSmallFontSize = mFontSize - 3;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weibo, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeiboStatus = ((WeiboActivity) getActivity()).getWeiboStatus();
        initLayout();
        buildLayout();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mTime = (TextView) view.findViewById(R.id.time);
        mSource = (TextView) view.findViewById(R.id.source);
        mText = (TextView) view.findViewById(R.id.text);
        mRetweetText = (TextView) view.findViewById(R.id.retweet_text);
        mPicture = (WeiboDetailPicView) view.findViewById(R.id.pic);
        mRetweetPicture = (WeiboDetailPicView) view.findViewById(R.id.retweet_pic);
        mPictureMulti = (GridLayout) view.findViewById(R.id.pic_multi);
        mRetweetPictureMulti = (GridLayout) view.findViewById(R.id.retweet_pic_multi);
        mRetweetLayout = (RelativeLayout) view.findViewById(R.id.retweet);
    }
    
    private void initLayout() {
        mUserName.setTextSize(mFontSize);
        mTime.setTextSize(mSmallFontSize);
        mSource.setTextSize(mSmallFontSize);
        mText.setTextSize(mFontSize);
        mRetweetText.setTextSize(mFontSize);
        mUserName.getPaint().setFakeBoldText(true);
        mText.setOnTouchListener(mTextOnTouchListener);
        mRetweetText.setOnTouchListener(mTextOnTouchListener);
        mRetweetText.setOnClickListener(mTextOnClickListener);
    }
    
    private void buildLayout() {
        WeiboUser user = mWeiboStatus.weiboUser;
        if (user != null) {
            mUserName.setText(user.name);
            mAvatar.setOnClickListener(new OnAvatarClickListener(user));
        }
        String time = TimeUtils.getListTime(mWeiboStatus);
        mTime.setText(time);
        if (!TextUtils.isEmpty(mWeiboStatus.source)) {
            mSource.setText(Html.fromHtml(mWeiboStatus.source).toString());
        }
        if (TextUtils.isEmpty(mWeiboStatus.textSpannable)) {
            mWeiboStatus.textSpannable = TextUtils.addWeiboLinks(mWeiboStatus.text);
        }
        mText.setText(mWeiboStatus.textSpannable);
        ImageDownloader.downloadAvatar(mAvatar, mWeiboStatus.weiboUser, false, mAvatarType);
        if (mWeiboStatus.thumbnailPic != null && mWeiboStatus.picCount > 0) {
            if (mWeiboStatus.picCount == 1) {
                mPicture.setVisibility(View.VISIBLE);
                buildPicture(mPicture, mWeiboStatus);
            } else {
                mPictureMulti.setVisibility(View.VISIBLE);
                buildMultiPicture(mPictureMulti, mWeiboStatus);
            }
        } else {
            if (mWeiboStatus.retweetStatus != null) {
                mRetweetLayout.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(mWeiboStatus.retweetStatus.textSpannable)) {
                    user = mWeiboStatus.retweetStatus.weiboUser;
                    String text;
                    if (user != null) {
                        text = "@" + user.name + " : " + mWeiboStatus.retweetStatus.text;
                    } else {
                        text = mWeiboStatus.retweetStatus.text;
                    }
                    mWeiboStatus.retweetStatus.textSpannable = TextUtils.addWeiboLinks(text);
                }
                mRetweetText.setText(mWeiboStatus.retweetStatus.textSpannable);
                if (mWeiboStatus.retweetStatus.thumbnailPic != null && mWeiboStatus.retweetStatus.picCount > 0) {
                    if (mWeiboStatus.retweetStatus.picCount == 1) {
                        mRetweetPicture.setVisibility(View.VISIBLE);
                        buildPicture(mRetweetPicture, mWeiboStatus.retweetStatus);
                    } else {
                        mRetweetPictureMulti.setVisibility(View.VISIBLE);
                        buildMultiPicture(mRetweetPictureMulti, mWeiboStatus.retweetStatus);
                    }
                }
            }
        }
    }
    
    private void buildPicture(WeiboDetailPicView view, WeiboStatus status) {
        String url;
        switch (mPictureType) {
        case PICTURE_MEDIUM:
            url = status.bmiddlePic[0];
            break;
        case PICTURE_LARGE:
            url = status.originalPic[0];
            break;
        default:
            throw new IllegalStateException("Illegal image type");
        }
        new WeiboDetailPictureReadTask(url, mPictureType, view).execute();
        view.setOnClickListener(new OnPictureClickListener(0, status));
    }
    
    private void buildMultiPicture(GridLayout grid, WeiboStatus status) {
        for (int i = 0; i < status.picCount; i++) {
            ImageView view = (ImageView) grid.getChildAt(i);
            view.setVisibility(View.VISIBLE);
            ImageDownloader.downloadMultiPicture(view, status, false, mPictureType, i);
            view.setOnClickListener(new OnPictureClickListener(i, status));
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
    
    private View.OnTouchListener mTextOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            TextView textView = (TextView) v;
            Layout layout = textView.getLayout();
            int x = (int) event.getX();
            int y = (int) event.getY();
            int offset = 0;
            if (layout != null) {
                int line = layout.getLineForVertical(y);
                offset = layout.getOffsetForHorizontal(line, x);
            }
            SpannableString text = SpannableString.valueOf(textView.getText());
            LongClickableLinkMovementMethod.getInstance().onTouchEvent(textView, text, event);
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                WeiboTextUrlSpan[] spans = text.getSpans(0, text.length(), WeiboTextUrlSpan.class);
                boolean found = false;
                int foundStart = 0;
                int foundEnd = 0;
                for (WeiboTextUrlSpan span : spans) {
                    int start = text.getSpanStart(span);
                    int end = text.getSpanEnd(span);
                    if (offset >= start && offset <= end) {
                        found = true;
                        foundStart = start;
                        foundEnd = end;
                        break;
                    }
                }
                boolean consumeEvent = false;
                if (found) {
                    consumeEvent = true;
                }
                if (found && !consumeEvent) {
                    clearBackgroundColorSpans(text, textView);
                }
                if (consumeEvent) {
                    BackgroundColorSpan span =
                            new BackgroundColorSpan(Utilities.getColor(R.attr.link_pressed_background_color));
                    text.setSpan(span, foundStart, foundEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    textView.setText(text);
                }
                return consumeEvent;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                clearBackgroundColorSpans(text, textView);
                break;
            }
            return false;
        }
        
        private void clearBackgroundColorSpans(SpannableString text, TextView textView) {
            BackgroundColorSpan[] spans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                text.removeSpan(span);
                textView.setText(text);
            }
        }
    };
    
    private View.OnClickListener mTextOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WeiboActivity.class);
            intent.putExtra(WeiboActivity.WEIBO_STATUS, mWeiboStatus.retweetStatus);
            getActivity().startActivity(intent);
        }
    };
    
    private class OnPictureClickListener implements View.OnClickListener {
        private int mPosition;
        private WeiboStatus mStatus;
        
        public OnPictureClickListener(int position, WeiboStatus status) {
            mPosition = position;
            mStatus = status;
        }
        
        @Override
        public void onClick(View v) {
            ImageViewerActivity.start(mStatus, mPosition, getActivity());
        }
    }
    
    private class OnAvatarClickListener implements View.OnClickListener {
        private WeiboUser mUser;
        
        public OnAvatarClickListener(WeiboUser user) {
            mUser = user;
        }
        
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), UserActivity.class);
            intent.putExtra(UserActivity.USER, mUser);
            getActivity().startActivity(intent);
        }
    }
}
