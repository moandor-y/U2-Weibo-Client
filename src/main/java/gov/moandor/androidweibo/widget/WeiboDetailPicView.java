package gov.moandor.androidweibo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    /*
    private static final String HTML = "<html>" + "    <head>" + "        <style>"
            + "            html,body{margin:0;padding:0;}" + "        </style>" + "    </head>" + "    <body>"
            + "        <img src=\"file://%s\" width=\"100%%\">" + "    </body>" + "</html>";
    */

    //private ImageWebView mImageWebView;
    //private ImageView mImageView;
    private GifImageView mGifView;
    private ProgressBar mProgressBar;
    private Button mRetryButton;

    public WeiboDetailPicView(Context context) {
        super(context);
        initLayout(context);
    }

    public WeiboDetailPicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public WeiboDetailPicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(context);
    }

    private static ViewGroup.LayoutParams resizeView(View view, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int margin = GlobalContext.getInstance().getResources().getDimensionPixelSize(R.dimen.margin_horizontal) * 2;
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
        //mImageWebView = (ImageWebView) view.findViewById(R.id.image_web);
        //mImageView = (ImageView) view.findViewById(R.id.image);
        mGifView = (GifImageView) view.findViewById(R.id.gif);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mRetryButton = (Button) view.findViewById(R.id.button_retry);
        //mImageWebView.setBackgroundColor(Color.TRANSPARENT);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public Button getRetryButton() {
        return mRetryButton;
    }

    /*
    public ImageWebView getImageWebView() {
        return mImageWebView;
    }
    */

    public GifImageView getImageView() {
        return mGifView;
    }

    /*
    public ImageView getImageView() {
        return mImageView;
    }
    */

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
        /*
        if (path.endsWith(".gif")) {
            setGif(path);
        } else {
            ViewGroup.LayoutParams params = resizeView(mImageView, path);
            Bitmap bitmap = ImageUtils.getBitmapFromFile(path, params.width, params.height);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(bitmap);
        }
        */
    }

    /*
    private void setGif(String path) {
        resizeView(mImageWebView, path);
        mImageWebView.setVisibility(View.VISIBLE);
        String data = String.format(HTML, path);
        mImageWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
    }
    */
}
