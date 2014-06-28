package gov.moandor.androidweibo.concurrency;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.ImageUtils;
import uk.co.senab.photoview.PhotoView;

public class ImageViewerPictureReadTask extends MyAsyncTask<Void, Integer, Boolean> {
    private static final String HTML = "<html>" + "    <head>"
            + "        <style>html,body{margin:0;padding:0;}</style>" + "    </head>" + "    <body>"
            + "        <table style=\"width: 100%%;height:100%%;\">" + "            <tr style=\"width: 100%%;\">"
            + "                <td valign=\"middle\" align=\"center\" style=\"width: 100%%;\">"
            + "                    <div style=\"display:block\">"
            + "                        <img src=\"file://%s\" width=\"100%%\" /" + "                    </div>"
            + "                </td>" + "            </tr>" + "        </table>" + "    </body>" + "</html>";

    private boolean mUseWebView;
    private String mUrl;
    private String mPath;
    private ImageDownloader.ImageType mType;
    private WebView mWebView;
    private PhotoView mPhotoView;
    private ProgressBar mProgressBar;
    private Button mRetryButton;
    private Bitmap mBitmap;
    private HttpUtils.DownloadListener mDownloadListener = new HttpUtils.DownloadListener() {
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
    };

    public ImageViewerPictureReadTask(String url, ImageDownloader.ImageType type, WebView webView, PhotoView photoView,
                                      ProgressBar progressBar, Button retryButton) {
        mUrl = url;
        mType = type;
        mWebView = webView;
        mPhotoView = photoView;
        mProgressBar = progressBar;
        mRetryButton = retryButton;
    }

    @Override
    protected void onPreExecute() {
        mProgressBar.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mPath = FileUtils.getImagePathFromUrl(mUrl, mType);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = ImageDownloadTaskCache.waitForPictureDownload(mUrl, mDownloadListener, mType);
        if (mPath.endsWith(".gif") || ImageUtils.isTooLargeToDisplay(mPath)) {
            mUseWebView = true;
        } else {
            mUseWebView = false;
            mBitmap = ImageUtils.getBitmapFromFile(mPath, ImageUtils.MAX_DISPLAY_SIZE, ImageUtils.MAX_DISPLAY_SIZE);
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mProgressBar.setProgress(values[0]);
        mProgressBar.setMax(values[1]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mProgressBar.setVisibility(View.GONE);
        if (result) {
            if (mUseWebView) {
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null, String.format(HTML, mPath), "text/html", "utf-8", null);
            } else {
                mPhotoView.setVisibility(View.VISIBLE);
                mPhotoView.setImageBitmap(mBitmap);
            }
        } else {
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewerPictureReadTask task =
                            new ImageViewerPictureReadTask(mUrl, mType, mWebView, mPhotoView, mProgressBar,
                                    mRetryButton);
                    task.execute();
                }
            });
        }
    }
}
