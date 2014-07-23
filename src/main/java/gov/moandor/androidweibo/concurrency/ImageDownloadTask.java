package gov.moandor.androidweibo.concurrency;

import java.util.concurrent.CopyOnWriteArrayList;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.ImageUtils;

public class ImageDownloadTask extends MyAsyncTask<Void, Integer, Boolean> {
    private String mUrl;
    private ImageDownloader.ImageType mType;
    private CopyOnWriteArrayList<HttpUtils.DownloadListener> mListeners =
            new CopyOnWriteArrayList<HttpUtils.DownloadListener>();

    public ImageDownloadTask(String url, ImageDownloader.ImageType type) {
        mUrl = url;
        mType = type;
    }

    @Override
    protected Boolean doInBackground(Void... v) {
        if (isCancelled()) {
            return false;
        }
        String path = FileUtils.getImagePathFromUrl(mUrl, mType);
        boolean result = ImageUtils.getBitmapFromNetwork(mUrl, path, new HttpUtils.DownloadListener() {
            @Override
            public void onPushProgress(int progress, int max) {
                publishProgress(progress, max);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onCancelled() {
            }
        });
        ImageDownloadTaskCache.removeImageDownloadTask(mUrl, this);
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        for (HttpUtils.DownloadListener l : mListeners) {
            l.onPushProgress(values[0], values[1]);
        }
    }

    public void addDownloadListener(HttpUtils.DownloadListener l) {
        mListeners.addIfAbsent(l);
    }
}
