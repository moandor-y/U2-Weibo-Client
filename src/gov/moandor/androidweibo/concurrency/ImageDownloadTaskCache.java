package gov.moandor.androidweibo.concurrency;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;

import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImageDownloadTaskCache {
    private static ConcurrentHashMap<String, ImageDownloadTask> mTasks =
            new ConcurrentHashMap<String, ImageDownloadTask>();
    static final Object backgroundWifiDownloadLock = new Object();
    
    public static boolean waitForPictureDownload(String url, HttpUtils.DownloadListener downloadListener,
            ImageDownloader.ImageType type) {
        while (true) {
            ImageDownloadTask task = mTasks.get(url);
            boolean fileExists = new File(FileUtils.getImagePathFromUrl(url, type)).exists();
            if (task == null) {
                if (!fileExists) {
                    ImageDownloadTask newTask = new ImageDownloadTask(url, type);
                    synchronized (backgroundWifiDownloadLock) {
                        task = mTasks.putIfAbsent(url, newTask);
                    }
                    if (task == null) {
                        task = newTask;
                        task.executeOnExecutor(MyAsyncTask.DOWNLOAD_THREAD_POOL_EXECUTOR);
                    }
                } else {
                    return true;
                }
            }
            if (downloadListener != null) {
                task.addDownloadListener(downloadListener);
            }
            try {
                return task.get(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Logger.logException(e);
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                Logger.logException(e);
                return false;
            } catch (TimeoutException e) {
                Logger.logException(e);
                return false;
            } catch (CancellationException e) {
                removeImageDownloadTask(url, task);
            }
        }
    }
    
    public static void removeImageDownloadTask(String url, ImageDownloadTask task) {
        synchronized (backgroundWifiDownloadLock) {
            mTasks.remove(url, task);
            if (isDownloadTaskFinished()) {
                backgroundWifiDownloadLock.notifyAll();
            }
        }
    }
    
    static boolean isDownloadTaskFinished() {
        return mTasks.isEmpty();
    }
}
