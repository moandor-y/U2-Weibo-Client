package gov.moandor.androidweibo.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import java.util.Locale;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.notification.ConnectivityChangeReceiver;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.UpdateFollowingIdsTask;
import gov.moandor.androidweibo.util.Utilities;

/**
 * SettingsActivityOldApi
 * Created by Moandor on 8/5/2014.
 */
@SuppressWarnings("deprecation")
public class SettingsActivityOldApi extends PreferenceActivity implements SharedPreferences
        .OnSharedPreferenceChangeListener {
    private static final String STATE_NEED_RESTART = "state_need_restart";
    private static final String NEED_RESTART = Utilities.buildIntentExtraName("NEED_RESTART");

    private boolean mNeedRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        mNeedRestart = getIntent().getBooleanExtra(NEED_RESTART, false);
        if (savedInstanceState != null) {
            mNeedRestart = savedInstanceState.getBoolean(STATE_NEED_RESTART);
        }
        ConfigManager.getPreferences().registerOnSharedPreferenceChangeListener(this);
        buildSummaries();
        bindClickPreference(this, SettingsActivity.KEY_NOTIFICATIONS, NotificationsActivity.class);
        bindClickPreference(this, SettingsActivity.KEY_IGNORE, IgnoreActivity.class);
        bindClickPreference(this, SettingsActivity.KEY_BLACK_MAGIC, BlackMagicActivity.class);
        bindClickPreference(this, SettingsActivity.KEY_ABOUT, AboutActivity.class);
        if (!ConfigManager.isBmEnabled()) {
            PreferenceCategory advanced = (PreferenceCategory) findPreference(SettingsActivity
                    .KEY_ADVANCED);
            Preference preference = findPreference(SettingsActivity.KEY_BLACK_MAGIC);
            advanced.removePreference(preference);
        }
        CheckBoxPreference listHwAccel = (CheckBoxPreference) findPreference(ConfigManager
                .LIST_HW_ACCEL_ENABLED);
        listHwAccel.setChecked(false);
        listHwAccel.setEnabled(false);
        CheckBoxPreference picHwAccel = (CheckBoxPreference) findPreference(ConfigManager
                .PIC_HW_ACCEL_ENABLED);
        picHwAccel.setChecked(false);
        picHwAccel.setEnabled(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_NEED_RESTART, mNeedRestart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConfigManager.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        requestRestart();
        if (key.equals(ConfigManager.THEME)) {
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), SettingsActivityOldApi.class);
            finish();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NEED_RESTART, true);
            startActivity(intent);
            overridePendingTransition(R.anim.stay, R.anim.activity_fade_out);
        } else {
            buildSummaries();
        }
    }

    private void exit() {
        if (mNeedRestart) {
            exitAndRestartMainActivity();
        } else {
            finish();
        }
    }

    private void exitAndRestartMainActivity() {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void requestRestart() {
        if (!mNeedRestart) {
            mNeedRestart = true;
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
        ListPreference preference = (ListPreference) findPreference(ConfigManager
                .LOAD_WEIBO_COUNT_MODE);
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
        ListPreference preference = (ListPreference) findPreference(ConfigManager
                .PICTURE_WIFI_QUALITY);
        preference.setSummary(preference.getEntry());
    }

    private void buildComRepAvatarSummary() {
        ListPreference preference = (ListPreference) findPreference(ConfigManager
                .COMMENT_REPOST_LIST_AVATAR_MODE);
        preference.setSummary(preference.getEntry());
    }

    private static void bindClickPreference(PreferenceActivity activity, String key,
            Class<?> activityClass) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), activityClass);
        activity.findPreference(key).setIntent(intent);
    }

    public static class NotificationsActivity extends PreferenceActivity implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        private static final int REQUEST_RINGTONE = 0;

        private Uri mRingtoneUri;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_notifications);
            buildSummaries();
            findPreference(ConfigManager.NOTIFICATION_RINGTONE).setOnPreferenceClickListener(
                    new OnRingtoneClickListener());
            String ringtone = ConfigManager.getNotificationRingtone();
            if (!TextUtils.isEmpty(ringtone)) {
                mRingtoneUri = Uri.parse(ringtone);
            }
            if (!ConfigManager.isBmEnabled()) {
                PreferenceCategory unread = (PreferenceCategory) findPreference(SettingsActivity
                        .KEY_UNREAD_MESSAGES);
                unread.removePreference(findPreference(ConfigManager.NOTIFICATION_DM_ENABLED));
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
        protected void onDestroy() {
            super.onDestroy();
            ConnectivityChangeReceiver.judgeAlarm(GlobalContext.getInstance());
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                preference.setSummary(RingtoneManager.getRingtone(this,
                        ringtoneUri).getTitle(this));
            } else {
                preference.setSummary(R.string.mute);
            }
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
    }

    public static class BlackMagicActivity extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_bm);
            findPreference(SettingsActivity.KEY_UPDATE_FOLLOWING).setOnPreferenceClickListener(new
                    OnUpdateFollowingClickListener());
        }

        private class OnUpdateFollowingClickListener implements Preference.OnPreferenceClickListener {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UpdateFollowingIdsTask task = new UpdateFollowingIdsTask();
                task.setOnUpdateFinishedListener(new OnUpdateFollowingFinishedListener());
                task.execute();
                Utilities.notice(R.string.updating);
                return true;
            }
        }

        private class OnUpdateFollowingFinishedListener implements UpdateFollowingIdsTask.OnUpdateFinishedListener {
            @Override
            public void onUpdateFinidhed() {
                Utilities.notice(R.string.update_finished);
            }
        }
    }

    public static class AboutActivity extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_about);
            buildMemoryInfo(findPreference(SettingsActivity.KEY_MEMORY));
            buildOfficialAccount(findPreference(SettingsActivity.KEY_OFFICIAL_ACCOUNT));
            buildDevelopers();
            buildDirectories();
            buildVersion();
            bindClickPreference(this, SettingsActivity.KEY_LICENSES,
                    SettingsActivity.LicensesActivity.class);
        }

        private static void buildMemoryInfo(Preference preference) {
            Runtime runtime = Runtime.getRuntime();
            long vmAlloc = runtime.totalMemory() - runtime.freeMemory();
            long nativeAlloc = Debug.getNativeHeapAllocatedSize();
            Context context = GlobalContext.getInstance();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = manager.getMemoryClass();
            String summary =
                    context.getString(R.string.vm_alloc_mem, formatMemoryText(vmAlloc) + " / " + memoryClass + " MB")
                            + "\n" + context.getString(R.string.native_alloc_mem, formatMemoryText(nativeAlloc));
            preference.setSummary(summary);
        }

        private void buildVersion() {
            Preference preference = findPreference(SettingsActivity.KEY_VERSION);
            preference.setSummary(Utilities.getVersionName());
        }

        private void buildOfficialAccount(Preference preference) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(ActivityUtils.userActivity(SettingsActivity.OFFICIAL_ACCOUNT));
                    return true;
                }
            });
        }

        private void buildDevelopers() {
            findPreference(SettingsActivity.KEY_DEVELOPER_1).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(ActivityUtils.userActivity(SettingsActivity.DEVELOPER_1));
                    return true;
                }
            });
            findPreference(SettingsActivity.KEY_DEVELOPER_2).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(ActivityUtils.userActivity(SettingsActivity.DEVELOPER_2));
                    return true;
                }
            });
        }

        private void buildDirectories() {
            findPreference(ConfigManager.PICTURE_CACHE_DIR).setSummary(ConfigManager
                    .getPictureCacheDir());
            findPreference(ConfigManager.AVATAR_CACHE_DIR).setSummary(ConfigManager
                    .getAvatarCacheDir());
            findPreference(SettingsActivity.KEY_DIR_LOGS).setSummary(FileUtils.LOGS);
        }

        private static String formatMemoryText(long memory) {
            float memoryInMB = (float) memory / (1024 * 1024);
            return String.format(Locale.ENGLISH, "%.1f MB", memoryInMB);
        }
    }
}
