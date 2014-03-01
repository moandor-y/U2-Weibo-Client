package gov.moandor.androidweibo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.HashMap;
import java.util.Map;

public class AuthorizeActivity extends AbsActivity {
    private static final String HACK_LOGIN_DIALOG = "hack_login_dialog";
    
    private WebView mWebView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);
        if (isHackLoginEnabled()) {
            HackLoginDialogFragment dialog = new HackLoginDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), HACK_LOGIN_DIALOG);
        } else {
            buildLayout();
        }
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
        if (mWebView!= null && isFinishing()) {
            mWebView.stopLoading();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView!= null) {
            mWebView.clearCache(true);
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }
    
    private void onHackLoginConfirmed() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), HackLoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    private boolean isHackLoginEnabled() {
        return getResources().getBoolean(R.bool.hack_enabled);
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
            try {
                Utilities.fetchAndSaveAccountInfo(mToken);
                Utilities.notice(R.string.auth_success);
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } catch (final WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
            }
        }
    }
    
    public static class HackLoginDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.hack_login);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AuthorizeActivity) getActivity()).onHackLoginConfirmed();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AuthorizeActivity) getActivity()).buildLayout();
                }
            });
            return builder.create();
        }
    }
}
