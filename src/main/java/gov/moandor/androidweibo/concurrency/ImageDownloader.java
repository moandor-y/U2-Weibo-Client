package gov.moandor.androidweibo.concurrency;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.widget.TimelinePicImageView;

public class ImageDownloader {
    static final Object sPauseImageReadTaskLock = new Object();
    static volatile boolean sPauseImageReadTask;
    private static WeakReference<Drawable> mImagePlaceHolder;

    public static void downloadMultiPicture(ImageView view, WeiboStatus status, boolean isFling, ImageType type,
                                            int picIndex) {
        String url;
        switch (type) {
            case PICTURE_SMALL:
                url = status.thumbnailPic[picIndex];
                break;
            case PICTURE_MEDIUM:
                url = status.bmiddlePic[picIndex];
                break;
            case PICTURE_LARGE:
                url = status.originalPic[picIndex];
                break;
            default:
                throw new IllegalArgumentException("Argument 'type' must be picture type");
        }
        displayImage(view, url, type, isFling, true);
    }

    public static void downloadTimelinePicture(TimelinePicImageView view, WeiboStatus status, boolean isFling,
                                               ImageType type) {
        String url;
        switch (type) {
            case PICTURE_SMALL:
                url = status.thumbnailPic[0];
                break;
            case PICTURE_MEDIUM:
                url = status.bmiddlePic[0];
                break;
            case PICTURE_LARGE:
                url = status.originalPic[0];
                break;
            default:
                throw new IllegalArgumentException("Argument 'type' must be picture type");
        }
        view.setIsGif(url.endsWith(".gif"));
        displayImage(view, url, type, isFling, false);
    }

    public static void downloadAvatar(ImageView view, WeiboUser user, boolean isFling, ImageType type) {
        if (user == null) {
            view.setImageBitmap(null);
            return;
        }
        String url;
        switch (type) {
            case AVATAR_SMALL:
                url = user.profileImageUrl;
                break;
            case AVATAR_LARGE:
                url = user.avatarLargeUrl;
                break;
            default:
                throw new IllegalStateException("Argument 'type' must be avatar type");
        }
        displayImage(view, url, type, isFling, false);
    }

    private static void displayImage(final ImageView view, String url, ImageType type, boolean isFling,
                                     boolean isMultiPictures) {
        view.clearAnimation();
        if (!shouldReloadPicture(view, url)) {
            return;
        }
        Bitmap bitmap = getBitmapFromMemCache(url);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            view.setTag(url);
            cancelPotentialDownload(view, url);
        } else {
            if (isFling) {
                Drawable drawable;
                if (mImagePlaceHolder == null || (drawable = mImagePlaceHolder.get()) == null) {
                    drawable = new ColorDrawable(Utilities.getColor(R.attr.image_place_holder));
                    mImagePlaceHolder = new WeakReference<Drawable>(drawable);
                }
                view.setImageDrawable(drawable);
                return;
            }
            if (!cancelPotentialDownload(view, url)) {
                return;
            }
            final ImageReadTask task = new ImageReadTask(url, type, view, isMultiPictures);
            AsyncDrawable asyncDrawable = new AsyncDrawable(task);
            view.setImageDrawable(asyncDrawable);
            GlobalContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ImageReadTask.getImageReadTask(view) == task) {
                        task.execute();
                    }
                }
            }, 400);
        }
    }

    private static boolean shouldReloadPicture(ImageView view, String url) {
        if (url.equals(view.getTag()) && view.getDrawable() != null && view.getDrawable() instanceof BitmapDrawable
                && (BitmapDrawable) view.getDrawable() != null
                && ((BitmapDrawable) view.getDrawable()).getBitmap() != null) {
            return false;
        } else {
            view.setTag(null);
            return true;
        }
    }

    private static Bitmap getBitmapFromMemCache(String urlKey) {
        return GlobalContext.getBitmapCache().get(urlKey);
    }

    private static boolean cancelPotentialDownload(ImageView view, String url) {
        ImageReadTask task = ImageReadTask.getImageReadTask(view);
        if (task != null) {
            String bitmapUrl = task.getUrl();
            if (bitmapUrl == null || !bitmapUrl.equals(url)) {
                task.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static void setPauseImageReadTask(boolean pause) {
        synchronized (sPauseImageReadTaskLock) {
            sPauseImageReadTask = pause;
            if (!sPauseImageReadTask) {
                sPauseImageReadTaskLock.notifyAll();
            }
        }
    }

    public static enum ImageType {
        AVATAR_SMALL, AVATAR_LARGE, PICTURE_SMALL, PICTURE_MEDIUM, PICTURE_LARGE
    }
}
