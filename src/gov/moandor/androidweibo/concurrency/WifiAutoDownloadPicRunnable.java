package gov.moandor.androidweibo.concurrency;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiAutoDownloadPicRunnable implements Runnable {
    private List<WeiboStatus> mStatuses;
    
    public WifiAutoDownloadPicRunnable(List<WeiboStatus> statuses, int position) {
        mStatuses = statuses;
        mStatuses.subList(0, position).clear();
    }
    
    @Override
    public void run() {
        ImageDownloader.ImageType[] order = new ImageDownloader.ImageType[5];
        switch (Utilities.getListPictureType()) {
        case PICTURE_SMALL:
            order[0] = ImageDownloader.ImageType.PICTURE_SMALL;
            order[1] = ImageDownloader.ImageType.PICTURE_LARGE;
            order[2] = ImageDownloader.ImageType.PICTURE_MEDIUM;
            break;
        case PICTURE_MEDIUM:
            order[0] = ImageDownloader.ImageType.PICTURE_MEDIUM;
            order[1] = ImageDownloader.ImageType.PICTURE_LARGE;
            order[2] = ImageDownloader.ImageType.PICTURE_SMALL;
            break;
        case PICTURE_LARGE:
            order[0] = ImageDownloader.ImageType.PICTURE_LARGE;
            order[1] = ImageDownloader.ImageType.PICTURE_MEDIUM;
            order[2] = ImageDownloader.ImageType.PICTURE_SMALL;
            break;
        default:
            throw new IllegalStateException("wrong list picture type");
        }
        switch (Utilities.getAvatarType()) {
        case AVATAR_SMALL:
            order[3] = ImageDownloader.ImageType.AVATAR_SMALL;
            order[4] = ImageDownloader.ImageType.AVATAR_LARGE;
            break;
        case AVATAR_LARGE:
            order[3] = ImageDownloader.ImageType.AVATAR_LARGE;
            order[4] = ImageDownloader.ImageType.AVATAR_SMALL;
            break;
        default:
            throw new IllegalStateException("wrong list avatar type");
        }
        List<String> urls = new ArrayList<String>();
        Map<String, ImageDownloader.ImageType> types = new HashMap<String, ImageDownloader.ImageType>();
        for (ImageDownloader.ImageType type : order) {
            for (WeiboStatus status : mStatuses) {
                if (status.picCount > 0) {
                    switch (type) {
                    case PICTURE_SMALL:
                        for (String url : status.thumbnailPic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_SMALL);
                        }
                        break;
                    case PICTURE_MEDIUM:
                        for (String url : status.bmiddlePic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_MEDIUM);
                        }
                        break;
                    case PICTURE_LARGE:
                        for (String url : status.originalPic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_LARGE);
                        }
                        break;
                    default:
                        break;
                    }
                } else if (status.retweetStatus != null && status.retweetStatus.picCount > 0) {
                    switch (type) {
                    case PICTURE_SMALL:
                        for (String url : status.retweetStatus.thumbnailPic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_SMALL);
                        }
                        break;
                    case PICTURE_MEDIUM:
                        for (String url : status.retweetStatus.bmiddlePic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_MEDIUM);
                        }
                        break;
                    case PICTURE_LARGE:
                        for (String url : status.retweetStatus.originalPic) {
                            urls.add(url);
                            types.put(url, ImageDownloader.ImageType.PICTURE_LARGE);
                        }
                        break;
                    default:
                        break;
                    }
                }
                String url;
                switch (type) {
                case AVATAR_SMALL:
                    url = status.weiboUser.profileImageUrl;
                    urls.add(url);
                    types.put(url, ImageDownloader.ImageType.AVATAR_SMALL);
                    break;
                case AVATAR_LARGE:
                    url = status.weiboUser.avatarLargeUrl;
                    urls.add(url);
                    types.put(url, ImageDownloader.ImageType.AVATAR_LARGE);
                    break;
                default:
                    break;
                }
            }
        }
        for (String url : urls) {
            if (!GlobalContext.isInWifi() || Thread.currentThread().isInterrupted()) {
                return;
            }
            synchronized (ImageDownloadTaskCache.backgroundWifiDownloadLock) {
                while (!ImageDownloadTaskCache.isDownloadTaskFinished() && !Thread.currentThread().isInterrupted()) {
                    try {
                        ImageDownloadTaskCache.backgroundWifiDownloadLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            ImageDownloader.ImageType type = types.get(url);
            String path = FileUtils.getImagePathFromUrl(url, type);
            ImageDownloadTaskCache.waitForPictureDownload(url, null, path, type);
        }
    }
}
