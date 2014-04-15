package gov.moandor.androidweibo.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;

public abstract class AbsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ConfigManager.getAppTheme()) {
        case ConfigManager.THEME_LIGHT:
            setTheme(R.style.Theme_Weibo_Light);
            break;
        case ConfigManager.THEME_DARK:
            setTheme(R.style.Theme_Weibo_Dark);
            break;
        }
        super.onCreate(savedInstanceState);
        GlobalContext.setActivity(this);
        switch (ConfigManager.getScreenOrientation()) {
        case ConfigManager.ORIENTATION_USER:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            break;
        case ConfigManager.ORIENTATION_LANDSCAPE:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            break;
        case ConfigManager.ORIENTATION_PORTRAIT:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            break;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.setActivity(this);
    }
}
