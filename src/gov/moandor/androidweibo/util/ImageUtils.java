package gov.moandor.androidweibo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageUtils {
    public static final int MAX_WIDTH = 1000;
    public static final int MAX_HEIGHT = 2000;
    public static final int MAX_DISPLAY_SIZE = 2048;
    
    public static boolean getBitmapFromNetwork(String url, String path, HttpUtils.DownloadListener listener) {
        for (int i = 0; i < 3; i++) {
            if (HttpUtils.executeDownloadTask(url, path, listener)) {
                return true;
            }
        }
        return false;
    }
    
    public static Bitmap getBitmapFromFile(String path, int width, int height) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            return BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError e) {
            Logger.logExcpetion(e);
            return null;
        }
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        int roundedSize;
        if (inSampleSize <= 8) {
            roundedSize = 1;
            while (roundedSize < inSampleSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (inSampleSize + 7) / 8 * 8;
        }
        return roundedSize;
    }
    
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(GlobalContext.getInstance().getResources(), bitmap);
    }
    
    public static boolean isTooLargeToDisplay(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return (options.outWidth > MAX_DISPLAY_SIZE || options.outHeight > MAX_DISPLAY_SIZE);
    }
}
