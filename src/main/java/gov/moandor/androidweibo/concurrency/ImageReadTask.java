package gov.moandor.androidweibo.concurrency;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.ImageUtils;

public class ImageReadTask extends MyAsyncTask<Void, Integer, Bitmap> {
    private String mUrl;
    private ImageDownloader.ImageType mType;
    private WeakReference<ImageView> mViewRef;
    private boolean mIsMultiPictures;

    public ImageReadTask(String url, ImageDownloader.ImageType type, ImageView view, boolean isMultiPictures) {
        mUrl = url;
        mType = type;
        mViewRef = new WeakReference<ImageView>(view);
        mIsMultiPictures = isMultiPictures;
    }

    public static ImageReadTask getImageReadTask(ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            return ((AsyncDrawable) drawable).getTask();
        }
        return null;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        synchronized (ImageDownloader.sPauseImageReadTaskLock) {
            while (ImageDownloader.sPauseImageReadTask && !isCancelled()) {
                try {
                    ImageDownloader.sPauseImageReadTaskLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (isCancelled()) {
            return null;
        }
        boolean downloaded = ImageDownloadTaskCache.waitForPictureDownload(mUrl, null, mType);
        if (!downloaded) {
            return null;
        }
        int width = 0;
        int height = 0;
        Resources resources = GlobalContext.getInstance().getResources();
        switch (mType) {
            case AVATAR_SMALL:
            case AVATAR_LARGE:
                width = resources.getDimensionPixelSize(R.dimen.list_avatar_width);
                height = resources.getDimensionPixelSize(R.dimen.list_avatar_height);
                break;
            case PICTURE_SMALL:
            case PICTURE_MEDIUM:
            case PICTURE_LARGE:
                if (!mIsMultiPictures) {
                    width = resources.getDimensionPixelSize(R.dimen.list_pic_width);
                    height = resources.getDimensionPixelSize(R.dimen.list_pic_height);
                } else {
                    width = resources.getDimensionPixelSize(R.dimen.list_multi_pic_width);
                    height = resources.getDimensionPixelSize(R.dimen.list_multi_pic_height);
                }
        }
        synchronized (ImageDownloader.sPauseImageReadTaskLock) {
            while (ImageDownloader.sPauseImageReadTask && !isCancelled()) {
                try {
                    ImageDownloader.sPauseImageReadTaskLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (isCancelled()) {
            return null;
        }
        String path = FileUtils.getImagePathFromUrl(mUrl, mType);
        return ImageUtils.getBitmapFromFile(path, width, height);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        display(bitmap);
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        display(bitmap);
    }

    private void display(Bitmap bitmap) {
        ImageView view = mViewRef.get();
        if (view != null) {
            if (canDisplay(view)) {
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                    AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                    animation.setDuration(500);
                    view.startAnimation(animation);
                    view.setTag(mUrl);
                    GlobalContext.getBitmapCache().put(mUrl, bitmap);
                }
            }
        }
    }

    private boolean canDisplay(ImageView view) {
        return getImageReadTask(view) == this;
    }

    public String getUrl() {
        return mUrl;
    }
}
