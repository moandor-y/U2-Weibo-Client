package gov.moandor.androidweibo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class AuthorizeActivity extends AbsActivity {
    private static final String HACK_LOGIN_DIALOG = "hack_login_dialog";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);
        buildLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_authorize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.refresh:
                mWebView.loadUrl("about:blank");
                mWebView.loadUrl(getWeiboOAuthUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearCache(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null && isFinishing()) {
            mWebView.stopLoading();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void buildLayout() {
        setContentView(R.layout.activity_authorize);
        mWebView = (WebView) findViewById(R.id.web);
        mWebView.setWebViewClient(new AuthWebViewClient());
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        CookieSyncManager.createInstance(GlobalContext.getInstance());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        mWebView.loadUrl(getWeiboOAuthUrl());
    }

    private String getWeiboOAuthUrl() {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", UrlHelper.APPKEY);
        params.put("response_type", "code");
        params.put("redirect_uri", UrlHelper.AUTH_REDIRECT);
        return UrlHelper.OAUTH2_AUTHORIZE + "?" + Utilities.encodeUrl(params)
                + "&scope=friendships_groups_read,friendships_groups_write";
    }

    private void handleRedirectUrl(String url) {
        Map<String, String> values = Utilities.parseUrl(url);
        String error = values.get("error");
        String errorCode = values.get("error_code");
        if (error == null && errorCode == null) {
            String code = values.get("code");
            MyAsyncTask.execute(new GetAccountInfoRunnable(code));
        } else {
            int code = 0;
            try {
                code = Integer.parseInt(errorCode);
            } catch (Exception e) {
                Logger.logException(e);
            }
            Utilities.notice(WeiboException.getError(code, error));
        }
    }

    private class AuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith(UrlHelper.AUTH_REDIRECT)) {
                handleRedirectUrl(url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler,
                SslError error) {
            handler.proceed();
        }
    }

    private class GetAccountInfoRunnable implements Runnable {
        private String mCode;

        public GetAccountInfoRunnable(String code) {
            mCode = code;
        }

        @Override
        public void run() {
            try {
                Utilities.fetchAndSaveAccountInfo(mCode);
                Utilities.notice(R.string.auth_success);
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } catch (final WeiboException e) {
                Logger.logException(e);
                Utilities.notice(e.getMessage());
            }
        }
    }
}
