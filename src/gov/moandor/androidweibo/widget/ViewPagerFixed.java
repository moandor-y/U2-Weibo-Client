package gov.moandor.androidweibo.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import gov.moandor.androidweibo.util.Logger;

public class ViewPagerFixed extends ViewPager {
    public ViewPagerFixed(Context context) {
        super(context);
    }
    
    public ViewPagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Logger.logException(e);
        }
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Logger.logException(e);
        }
        return false;
    }
}
