package gov.moandor.androidweibo.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import gov.moandor.androidweibo.util.GlobalContext;

public class ImageWebView extends WebView {
    public ImageWebView(Context context) {
        super(context);
        initView();
    }
    
    public ImageWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    
    public ImageWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initView() {
        setFocusable(false);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (GlobalContext.isPicHwAccelEnabled()) {
                setLayerType(LAYER_TYPE_HARDWARE, null);
            } else {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        }
    }
}
