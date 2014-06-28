package gov.moandor.androidweibo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;

public class TimelinePicImageView extends ImageView {
    private static Bitmap sGifFlag = BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(),
            R.drawable.gif_flag);
    private boolean mIsGif;

    public TimelinePicImageView(Context context) {
        super(context);
    }

    public TimelinePicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimelinePicImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mIsGif) {
            int width = sGifFlag.getWidth();
            int height = sGifFlag.getHeight();
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            canvas.drawBitmap(sGifFlag, x, y, null);
        }
    }

    public void setIsGif(boolean isGif) {
        mIsGif = isGif;
    }
}
