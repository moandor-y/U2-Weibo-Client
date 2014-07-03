package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.WeiboMidToIdDao;
import gov.moandor.androidweibo.dao.WeiboStatusDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class IncomingUrlActivity extends AbsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getData().toString();
        if (Utilities.isWeiboMidUrl(url)) {
            String mid = Utilities.getMidFromUrl(url);
            new RedirectToWeiboActivityTask(mid).execute();
        } else {
            WebView web = new WebView(this);
            setContentView(web);
            web.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    finish();
                    return true;
                }
            });
            web.loadUrl(url);
        }
    }

    private class RedirectToWeiboActivityTask extends MyAsyncTask<Void, Void, WeiboStatus> {
        private String mMid;
        private String mToken;

        private RedirectToWeiboActivityTask(String mid) {
            mMid = mid;
        }

        @Override
        protected WeiboStatus doInBackground(Void... params) {
            WeiboMidToIdDao dao = new WeiboMidToIdDao();
            dao.setToken(mToken);
            dao.setMid(mMid);
            try {
                long id = dao.execute().longValue();
                WeiboStatusDao statusDao = new WeiboStatusDao();
                statusDao.setToken(mToken);
                statusDao.setId(id);
                return statusDao.execute();
            } catch (WeiboException e) {
                Logger.logException(e);
                Utilities.notice(e.getMessage());
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mToken = GlobalContext.getCurrentAccount().token;
        }

        @Override
        protected void onPostExecute(WeiboStatus result) {
            startActivity(ActivityUtils.weiboActivity(result));
            finish();
        }
    }
}
