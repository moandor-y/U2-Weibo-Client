package gov.moandor.androidweibo.concurrency;

import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.HttpUtils;

public class ImageViewerPictureReadTask extends MyAsyncTask<Void, Integer, Boolean> {
    private static final String HTML =
            "<html>" +
            "    <head>" +
            "        <style>html,body{margin:0;padding:0;}</style>" +
            "    </head>" +
            "    <body>" +
            "        <table style=\"width: 100%%;height:100%%;\">" +
            "            <tr style=\"width: 100%%;\">" +
            "                <td valign=\"middle\" align=\"center\" style=\"width: 100%%;\">" +
            "                    <div style=\"display:block\">" +
            "                        <img src=\"file://%s\" width=\"100%%\" /" +
            "                    </div>" +
            "                </td>" +
            "            </tr>" +
            "        </table>" +
            "    </body>" +
            "</html>";
    
    private String mUrl;
    private String mPath;
    private ImageDownloader.ImageType mType;
    private WebView mView;
    private ProgressBar mProgressBar;
    private Button mRetryButton;
    
    public ImageViewerPictureReadTask(String url, ImageDownloader.ImageType type, WebView view, 
            ProgressBar progressBar, Button retryButton) {
        mUrl = url;
        mType = type;
        mView = view;
        mProgressBar = progressBar;
        mRetryButton = retryButton;
    }
    
    @Override
    protected void onPreExecute() {
        mProgressBar.setVisibility(View.VISIBLE);
        mView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        mPath = FileUtils.getImagePathFromUrl(mUrl, mType);
        return ImageDownloadTaskCache.waitForPictureDownload(mUrl, mDownloadListener, mPath, mType);
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
            mView.setVisibility(View.VISIBLE);
            mView.loadDataWithBaseURL(null, String.format(HTML, mPath), "text/html", "utf-8", null);
        } else {
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageViewerPictureReadTask task = new ImageViewerPictureReadTask(mUrl, 
                            mType, mView, mProgressBar, mRetryButton);
                    task.execute();
                }
            });
        }
    }
    
    private HttpUtils.DownloadListener mDownloadListener = new HttpUtils.DownloadListener() {
        @Override
        public void onPushProgress(int progress, int max) {
            publishProgress(progress, max);
        }
        
        @Override
        public void onComplete() {}
        
        @Override
        public void onCancelled() {}
    };
}
