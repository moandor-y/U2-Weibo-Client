package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.Oauth2AcessTokenDao;
import gov.moandor.androidweibo.util.GlobalContext;
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
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this, R.array.hack_login_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                        Oauth2AcessTokenDao dao = new Oauth2AcessTokenDao();
                        dao.setUsername(username.getText().toString());
                        dao.setPassword(password.getText().toString());
                        dao.setClientId(getResources().getStringArray(R.array.hack_login_keys)[spinner.getSelectedItemPosition()]);
                        dao.setClientSecret(getResources().getStringArray(R.array.hack_login_secrets)[spinner.getSelectedItemPosition()]);
                        dao.setGrantType("password");
                        try {
                            String token = dao.execute();
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
