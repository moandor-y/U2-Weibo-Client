package gov.moandor.androidweibo.activity;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.notification.ConnectivityChangeReceiver;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    
    public static class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            buildSummaries();
        }
        
        @Override
        public void onResume() {
            super.onResume();
            ConfigManager.getPreferences().registerOnSharedPreferenceChangeListener(this);
        }
        
        @Override
        public void onPause() {
            super.onPause();
            ConfigManager.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(ConfigManager.THEME)) {
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), SettingsActivity.class);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.stay, R.anim.activity_fade_out);
            } else {
                buildSummaries();
            }
        }
        
        private void buildSummaries() {
            buildThemeSummary();
            buildFontSizeSummary();
            buildLoadCountSummary();
            buildAvatarSummary();
            buildPictureSummary();
            buildWifiPictureSummary();
            buildComRepAvatarSummary();
        }
        
        private void buildThemeSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.THEME);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildFontSizeSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.FONT_SIZE_MODE);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildLoadCountSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.LOAD_WEIBO_COUNT_MODE);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildAvatarSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.AVATAR_QUALITY);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildPictureSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.PICTURE_QUALITY);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildWifiPictureSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.PICTURE_WIFI_QUALITY);
            preference.setSummary(preference.getEntry());
        }
        
        private void buildComRepAvatarSummary() {
            ListPreference preference = (ListPreference) findPreference(ConfigManager.COMMENT_REPOST_LIST_AVATAR_MODE);
            preference.setSummary(preference.getEntry());
        }
    }
    
    public static class NotificationsActivity extends AbsActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentById(android.R.id.content);
            if (fragment == null) {
                fragment = new NotificationsFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(android.R.id.content, fragment);
                ft.commit();
            }
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.notifications);
        }
        
        @Override
        protected void onDestroy() {
            super.onDestroy();
            ConnectivityChangeReceiver.judgeAlarm(GlobalContext.getInstance());
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
        
        public static class NotificationsFragment extends PreferenceFragment implements
                SharedPreferences.OnSharedPreferenceChangeListener {
            private static final int REQUEST_RINGTONE = 0;
            
            private Uri mRingtoneUri;
            
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.prefs_notifications);
                buildSummaries();
                findPreference(ConfigManager.NOTIFICATION_RINGTONE).setOnPreferenceClickListener(new OnRingtoneClickListener());
                String ringtone = ConfigManager.getNotificationRingtone();
                if (!TextUtils.isEmpty(ringtone)) {
                    mRingtoneUri = Uri.parse(ringtone);
                }
            }
            
            @Override
            public void onResume() {
                super.onResume();
                ConfigManager.getPreferences().registerOnSharedPreferenceChangeListener(this);
            }
            
            @Override
            public void onPause() {
                super.onPause();
                ConfigManager.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
            }
            
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode != RESULT_OK) {
                    return;
                }
                switch (requestCode) {
                case REQUEST_RINGTONE:
                    mRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (mRingtoneUri != null) {
                        ConfigManager.setNotificationRingtone(mRingtoneUri.toString());
                    } else {
                        ConfigManager.setNotificationRingtone(null);
                    }
                    buildRingtoneSummary();
                }
            }
            
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                buildSummaries();
            }
            
            private class OnRingtoneClickListener implements Preference.OnPreferenceClickListener {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone));
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mRingtoneUri);
                    startActivityForResult(intent, REQUEST_RINGTONE);
                    return true;
                }
            }
            
            private void buildSummaries() {
                buildIntervalSummary();
                buildWifiIntervalSummary();
                buildRingtoneSummary();
            }
            
            private void buildIntervalSummary() {
                ListPreference preference = (ListPreference) findPreference(ConfigManager.NOTIFICATION_FREQUENCY);
                preference.setSummary(preference.getEntry());
            }
            
            private void buildWifiIntervalSummary() {
                ListPreference preference = (ListPreference) findPreference(ConfigManager.NOTIFICATION_FREQUENCY_WIFI);
                preference.setSummary(preference.getEntry());
            }
            
            private void buildRingtoneSummary() {
                Preference preference = findPreference(ConfigManager.NOTIFICATION_RINGTONE);
                String ringtone = ConfigManager.getNotificationRingtone();
                if (!TextUtils.isEmpty(ringtone)) {
                    Uri ringtoneUri = Uri.parse(ringtone);
                    preference.setSummary(RingtoneManager.getRingtone(getActivity(), ringtoneUri).getTitle(
                            getActivity()));
                } else {
                    preference.setSummary(R.string.mute);
                }
            }
        }
    }
}
