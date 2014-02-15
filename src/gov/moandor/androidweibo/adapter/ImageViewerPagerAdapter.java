package gov.moandor.androidweibo.adapter;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.ImageViewerPictureReadTask;
import gov.moandor.androidweibo.util.GlobalContext;

public class ImageViewerPagerAdapter extends PagerAdapter {
    private ImageDownloader.ImageType mImageType;
    private String[] mUrls;
    
    public ImageViewerPagerAdapter(ImageDownloader.ImageType imageType, String[] urls) {
        mImageType = imageType;
        mUrls = urls;
    }
    
    @Override
    public int getCount() {
        return mUrls.length;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
            if (GlobalContext.isPicHwAccelEnabled()) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setBackgroundColor(Color.TRANSPARENT);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        Button retryButton = (Button) view.findViewById(R.id.button_retry);
        ImageViewerPictureReadTask task = new ImageViewerPictureReadTask(url, mImageType, webView, 
                progressBar, retryButton);
        task.execute();
        container.addView(view);
        return view;
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof ViewGroup) {
            ((ViewPager) container).removeView((View) object);
        }
    }
}
