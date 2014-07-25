package gov.moandor.androidweibo.concurrency;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.widget.WeiboDetailPicView;
import pl.droidsonroids.gif.GifImageView;

public class WeiboDetailPictureReadTask extends MyAsyncTask<Void, Integer, Boolean> {
    private String mUrl;
    private String mPath;
    private ImageDownloader.ImageType mType;
    private WeiboDetailPicView mView;
    private GifImageView mImageView;
    //private ImageWebView mImageWebView;
    private Button mRetryButton;
    private ProgressBar mProgressBar;
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

    public WeiboDetailPictureReadTask(String url, ImageDownloader.ImageType type, WeiboDetailPicView view) {
        mUrl = url;
        mType = type;
        mView = view;
        mImageView = view.getImageView();
        //mImageWebView = view.getImageWebView();
        mRetryButton = view.getRetryButton();
        mProgressBar = view.getProgressBar();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        mPath = FileUtils.getImagePathFromUrl(mUrl, mType);
        return ImageDownloadTaskCache.waitForPictureDownload(mUrl, mDownloadListener, mType);
    }

    @Override
    protected void onPreExecute() {
        mImageView.setVisibility(View.GONE);
        //mImageWebView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mRetryButton.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mProgressBar.setVisibility(View.GONE);
        if (result) {
            mView.setImage(mPath);
        } else {
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new WeiboDetailPictureReadTask(mUrl, mType, mView).execute();
                }
            });
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressBar.isIndeterminate()) {
            mProgressBar.setIndeterminate(false);
        }
        mProgressBar.setProgress(values[0]);
        mProgressBar.setMax(values[1]);
    }
}
