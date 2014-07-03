package gov.moandor.androidweibo.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.ImageViewerPictureReadTask;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewerPagerAdapter extends PagerAdapter {
    private ImageDownloader.ImageType mImageType;
    private String[] mUrls;
    private Activity mActivity;

    public ImageViewerPagerAdapter(
            ImageDownloader.ImageType imageType, String[] urls, Activity activity) {
        mImageType = imageType;
        mUrls = urls;
        mActivity = activity;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void setupForNewApi(WebView webView, WebSettings settings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
            if (!ConfigManager.isPicHwAccelEnabled()) {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
    }

    @Override
    public int getCount() {
        return mUrls.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = GlobalContext.getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.image_viewer_item, container, false);
        String url = mUrls[position];
        WebView webView = (WebView) view.findViewById(R.id.web);
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        setupForNewApi(webView, settings);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setBackgroundColor(Color.TRANSPARENT);
        final GestureDetector gestureDetector = new GestureDetector(mActivity,
                new OnGestureListener());
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        PhotoView photoView = (PhotoView) view.findViewById(R.id.photo);
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                mActivity.finish();
            }
        });
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        Button retryButton = (Button) view.findViewById(R.id.button_retry);
        ImageViewerPictureReadTask task =
                new ImageViewerPictureReadTask(url, mImageType, webView, photoView, progressBar,
                        retryButton);
        task.execute();
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof ViewGroup) {
            ((ViewPager) container).removeView((View) object);
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setImageType(ImageDownloader.ImageType type, String[] urls) {
        mImageType = type;
        mUrls = urls;
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mActivity.finish();
            return true;
        }
    }
}
