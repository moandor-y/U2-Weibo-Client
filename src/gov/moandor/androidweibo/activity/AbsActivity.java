package gov.moandor.androidweibo.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;

public abstract class AbsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (GlobalContext.getAppTheme()) {
        case GlobalContext.THEME_LIGHT:
            setTheme(R.style.Theme_Weibo_Light);
            break;
        case GlobalContext.THEME_DARK:
            setTheme(R.style.Theme_Weibo_Dark);
            break;
        }
        super.onCreate(savedInstanceState);
        GlobalContext.setActivity(this);
        switch (GlobalContext.getScreenOrientation()) {
        case GlobalContext.ORIENTATION_USER:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            break;
        case GlobalContext.ORIENTATION_LANDSCAPE:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            break;
        case GlobalContext.ORIENTATION_PORTRAIT:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            break;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.setActivity(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        GlobalContext.savePreferences();
    }
}
