package gov.moandor.androidweibo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.HashMap;
import java.util.Map;

public class AuthorizeActivity extends AbsActivity {
    private WebView mWebView;
    
    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.login);
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
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mWebView.stopLoading();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.clearCache(true);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            Toast.makeText(GlobalContext.getInstance(), R.string.you_cancelled_auth, Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getWeiboOAuthUrl() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", HttpUtils.UrlHelper.APPKEY);
        params.put("response_type", "token");
        params.put("redirect_uri", HttpUtils.UrlHelper.AUTH_REDIRECT);
        params.put("display", "mobile");
        return HttpUtils.UrlHelper.OAUTH2_AUTHORIZE + "?" + Utilities.encodeUrl(params)
                + "&scope=friendships_groups_read,friendships_groups_write";
    }
    
    private class AuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith(HttpUtils.UrlHelper.AUTH_REDIRECT)) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);
        }
        
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }
    
    private void handleRedirectUrl(WebView view, String url) {
        Map<String, String> values = Utilities.parseUrl(url);
        String error = values.get("error");
        String errorCode = values.get("error_code");
        if (error == null && errorCode == null) {
            String token = values.get("access_token");
            MyAsyncTask.execute(new GetAccountInfoRunnable(token));
        } else {
            int code = 0;
            try {
                code = Integer.parseInt(errorCode);
            } catch (Exception e) {
                Logger.logExcpetion(e);
            }
            Utilities.notice(WeiboException.getError(code, error));
        }
    }
    
    private class GetAccountInfoRunnable implements Runnable {
        private String mToken;
        
        public GetAccountInfoRunnable(String token) {
            mToken = token;
        }
        
        @Override
        public void run() {
            HttpParams params = new HttpParams();
            params.addParam("access_token", mToken);
            try {
                String response =
                        HttpUtils.executeNormalTask(HttpUtils.Method.GET, HttpUtils.UrlHelper.ACCOUNT_GET_UID, params);
                long id = Utilities.getWeiboAccountIdFromJson(response);
                params.clear();
                params.addParam("access_token", mToken);
                params.addParam("uid", String.valueOf(id));
                response = HttpUtils.executeNormalTask(HttpUtils.Method.GET, HttpUtils.UrlHelper.USERS_SHOW, params);
                Account account = new Account();
                account.token = mToken;
                account.user = Utilities.getWeiboUserFromJson(response);
                GlobalContext.addOrUpdateAccount(account);
                GlobalContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GlobalContext.getInstance(), R.string.auth_success, Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                startActivity(intent);
                finish();
            } catch (final WeiboException e) {
                Logger.logExcpetion(e);
                GlobalContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.notice(e.getMessage());
                    }
                });
            }
        }
    }
}
