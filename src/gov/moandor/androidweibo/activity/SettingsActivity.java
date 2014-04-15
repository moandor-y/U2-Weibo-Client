package gov.moandor.androidweibo.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.MainActivity;
import gov.moandor.androidweibo.util.GlobalContext;

public class SettingsActivity extends AbsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		Fragment fragment = fm.findFragmentById(android.R.id.content);
		if (fragment == null) {
			fragment = new SettingsFragment();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(android.R.id.content, fragment);
			ft.commit();
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            exit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	private void exit() {
		Intent intent = new Intent();
		intent.setClass(GlobalContext.getInstance(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
	
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
	
	public static class NotificationsActivity extends AbsActivity {
		
	}
}
