package gov.moandor.androidweibo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.IOException;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.ImageUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class WeiboDetailPicView extends FrameLayout {
    private boolean mRetweet;
    private GifImageView mGifView;
    private ProgressBar mProgressBar;
    private Button mRetryButton;

    public WeiboDetailPicView(Context context) {
        super(context);
        initLayout(context);
    }

    public WeiboDetailPicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttributes(context, attrs);
        initLayout(context);
    }

    public WeiboDetailPicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        handleAttributes(context, attrs);
        initLayout(context);
    }

    private void handleAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.WeiboDetailPic, 0, 0);
        try {
            mRetweet = typedArray.getBoolean(R.styleable.WeiboDetailPic_retweet, false);
        } finally {
            typedArray.recycle();
        }
    }

    private ViewGroup.LayoutParams resizeView(View view, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int margin;
        if (mRetweet) {
            margin = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen
                    .margin_horizontal) * 2;
        } else {
            margin = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen
                    .margin_horizontal);
        }
        int width = Math.max(Utilities.dpToPx(200), options.outWidth);
        int maxWidth = Utilities.getScreenWidth() - margin * 2;
        width = Math.min(width, maxWidth);
        int height = width * options.outHeight / options.outWidth;
        int newHeight = Math.min(Utilities.getScreenHeight(), height);
        int newWidth = newHeight * width / height;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = newWidth;
        layoutParams.height = newHeight;
        view.requestLayout();
        return layoutParams;
    }

    private void initLayout(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_weibo_detail_pic, this, true);
        mGifView = (GifImageView) view.findViewById(R.id.gif);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mRetryButton = (Button) view.findViewById(R.id.button_retry);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public Button getRetryButton() {
        return mRetryButton;
    }

    public GifImageView getImageView() {
        return mGifView;
    }

    public void setImage(String path) {
        ViewGroup.LayoutParams params = resizeView(mGifView, path);
        try {
            Drawable drawable;
            if (path.endsWith(".gif")) {
                drawable = new GifDrawable(path);
            } else {
                Bitmap bitmap = ImageUtils.getBitmapFromFile(path, params.width, params.height);
                drawable = new BitmapDrawable(GlobalContext.getInstance().getResources(), bitmap);
            }
            mGifView.setVisibility(View.VISIBLE);
            mGifView.setImageDrawable(drawable);
        } catch (IOException e) {
            Logger.logException(e);
        }
    }
}
