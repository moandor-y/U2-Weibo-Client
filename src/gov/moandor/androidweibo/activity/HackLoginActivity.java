package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class HackLoginActivity extends AbsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_login);
        final Spinner spinner = (Spinner) findViewById(R.id.type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(GlobalContext.getInstance(), R.layout.main_spinner, 
                android.R.id.text1, getResources().getStringArray(R.array.hack_login_types));
        adapter.setDropDownViewResource(R.layout.main_navigation_spinner_item);
        spinner.setAdapter(adapter);
        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(username.getText())) {
                    username.setError(getString(R.string.username_empty));
                    return;
                }
                if (TextUtils.isEmpty(password.getText())) {
                    password.setError(getString(R.string.password_empty));
                    return;
                }
                MyAsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String url = HttpUtils.UrlHelper.OAUTH2_ACCESS_TOKEN;
                        HttpParams params = new HttpParams();
                        params.addParam("username", username.getText().toString());
                        params.addParam("password", password.getText().toString());
                        params.addParam("client_id", getResources().getStringArray(R.array.hack_login_keys)
                                [spinner.getSelectedItemPosition()]);
                        params.addParam("client_secret", getResources().getStringArray(R.array.hack_login_secrets)
                                [spinner.getSelectedItemPosition()]);
                        params.addParam("grant_type", "password");
                        String response;
                        try {
                            response = HttpUtils.executeNormalTask(HttpUtils.Method.POST, url, params);
                            JSONObject json = new JSONObject(response);
                            String token = json.getString("access_token");
                            Utilities.fetchAndSaveAccountInfo(token);
                            Utilities.notice(R.string.auth_success);
                            Intent intent = new Intent();
                            intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } catch (WeiboException e) {
                            Logger.logExcpetion(e);
                            Utilities.notice(e.getMessage());
                        } catch (JSONException e) {
                            Logger.logExcpetion(e);
                            Utilities.notice(R.string.json_error);
                        }
                    }
                });
            }
        });
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
