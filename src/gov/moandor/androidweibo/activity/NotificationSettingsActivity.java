package gov.moandor.androidweibo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.notification.ConnectivityChangeReceiver;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public class NotificationSettingsActivity extends AbsActivity {
    private static final int REQUEST_RINGTONE = 0;
    private static final String INTERVAL_DIALOG = "interval_dialog";
    
    private static boolean sNeedRestart;
    private TextView mIntervalStatus;
    private TextView mRingtoneStatus;
    private TextView mIntervalLabel;
    private TextView mRingtoneLabel;
    private CheckedTextView mMentionWeibo;
    private CheckedTextView mComment;
    private CheckedTextView mMentionComment;
    private CheckedTextView mVibrate;
    private CheckedTextView mNotificationLed;
    private View mIntervalLayout;
    private View mRingtoneLayout;
    private String[] mIntervals;
    private Uri mRingtoneUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        View view = getLayoutInflater().inflate(R.layout.action_toggle, null);
        CompoundButton action = (CompoundButton) view.findViewById(R.id.action_toggle);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.RIGHT);
        getSupportActionBar().setCustomView(action, params);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.notifications);
        action.setChecked(GlobalContext.isNotificationEnabled());
        action.setOnCheckedChangeListener(new OnActionCheckeChangeListener());
        mIntervals = getResources().getStringArray(R.array.notification_intervals);
        mIntervalStatus = (TextView) findViewById(R.id.interval_status);
        mRingtoneStatus = (TextView) findViewById(R.id.ringtone_status);
        mIntervalLabel = (TextView) findViewById(R.id.interval_label);
        mRingtoneLabel = (TextView) findViewById(R.id.ringtone_label);
        mMentionWeibo = (CheckedTextView) findViewById(R.id.mention_weibo);
        mComment = (CheckedTextView) findViewById(R.id.comment);
        mMentionComment = (CheckedTextView) findViewById(R.id.mention_comment);
        mVibrate = (CheckedTextView) findViewById(R.id.vibrate);
        mNotificationLed = (CheckedTextView) findViewById(R.id.notification_led);
        mIntervalLayout = findViewById(R.id.interval);
        mRingtoneLayout = findViewById(R.id.ringtone);
        mMentionWeibo.setOnClickListener(new OnMentionWeiboClickListener());
        mComment.setOnClickListener(new OnCommentClickListener());
        mMentionComment.setOnClickListener(new OnMentionCommentClickListener());
        mVibrate.setOnClickListener(new OnVibrateClickListener());
        mNotificationLed.setOnClickListener(new OnNotificationLedClickListener());
        mIntervalLayout.setOnClickListener(new OnIntervalClickListener());
        mRingtoneLayout.setOnClickListener(new OnRingtoneClickListener());
        setupViews();
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
                GlobalContext.setNotificationRingtone(mRingtoneUri.toString());
            } else {
                GlobalContext.setNotificationRingtone(null);
            }
            setupRingtone();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (sNeedRestart) {
            sNeedRestart = false;
            ConnectivityChangeReceiver.judgeAlarm(GlobalContext.getInstance());
        }
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
    
    private void setupViews() {
        setupInterval();
        setupMentionWeibo();
        setupComment();
        setupMentionComment();
        setupVibrate();
        setupNotificationLed();
        setupRingtone();
    }
    
    private void setupInterval() {
        boolean enabled = GlobalContext.isNotificationEnabled();
        mIntervalLayout.setEnabled(enabled);
        mIntervalLabel.setEnabled(enabled);
        mIntervalStatus.setEnabled(enabled);
        mIntervalStatus.setText(mIntervals[GlobalContext.getNotificationFrequency()]);
    }
    
    private void setupMentionWeibo() {
        mMentionWeibo.setEnabled(GlobalContext.isNotificationEnabled());
        mMentionWeibo.setChecked(GlobalContext.isNotificationMentionWeiboEnabled());
    }
    
    private void setupComment() {
        mComment.setEnabled(GlobalContext.isNotificationEnabled());
        mComment.setChecked(GlobalContext.isNotificationCommentEnabled());
    }
    
    private void setupMentionComment() {
        mMentionComment.setEnabled(GlobalContext.isNotificationEnabled());
        mMentionComment.setChecked(GlobalContext.isNotificationMentionCommentEnabled());
    }
    
    private void setupVibrate() {
        mVibrate.setEnabled(GlobalContext.isNotificationEnabled());
        mVibrate.setChecked(GlobalContext.isNotificationVibrateEnabled());
    }
    
    private void setupNotificationLed() {
        mNotificationLed.setEnabled(GlobalContext.isNotificationEnabled());
        mNotificationLed.setChecked(GlobalContext.isNotificationLedEnabled());
    }
    
    private void setupRingtone() {
        boolean enabled = GlobalContext.isNotificationEnabled();
        mRingtoneLayout.setEnabled(enabled);
        mRingtoneLabel.setEnabled(enabled);
        mRingtoneStatus.setEnabled(enabled);
        String ringtone = GlobalContext.getNotificationRingtone();
        if (!TextUtils.isEmpty(ringtone)) {
            mRingtoneUri = Uri.parse(ringtone);
            mRingtoneStatus.setText(RingtoneManager.getRingtone(this, mRingtoneUri).getTitle(this));
        } else {
            mRingtoneStatus.setText(R.string.mute);
        }
    }
    
    private static void requestRestart() {
        if (!sNeedRestart) {
            sNeedRestart = true;
        }
    }
    
    private class OnIntervalClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    SettingsActivityOldApi.buildListDialog(R.string.interval, mIntervals, GlobalContext
                            .getNotificationFrequency(), new OnIntervalSelectedListener(),
                            NotificationSettingsActivity.this);
            SettingsActivityOldApi.SettingsDialogFragment dialog = new SettingsActivityOldApi.SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), INTERVAL_DIALOG);
        }
    }
    
    private class OnMentionWeiboClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mMentionWeibo.toggle();
            GlobalContext.setNotificationMentionWeiboEnabled(mMentionWeibo.isChecked());
            requestRestart();
        }
    }
    
    private class OnCommentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mComment.toggle();
            GlobalContext.setNotificationCommentEnabled(mComment.isChecked());
            requestRestart();
        }
    }
    
    private class OnMentionCommentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mMentionComment.toggle();
            GlobalContext.setNotificationMentionCommentEnabled(mMentionComment.isChecked());
            requestRestart();
        }
    }
    
    private class OnVibrateClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mVibrate.toggle();
            GlobalContext.setNotificationVibrateEnabled(mVibrate.isChecked());
            requestRestart();
        }
    }
    
    private class OnNotificationLedClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mNotificationLed.toggle();
            GlobalContext.setNotificationLedEnabled(mNotificationLed.isChecked());
            requestRestart();
        }
    }
    
    private class OnRingtoneClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mRingtoneUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, mRingtoneUri);
            startActivityForResult(intent, REQUEST_RINGTONE);
        }
    }
    
    private class OnIntervalSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getNotificationFrequency()) {
                return;
            }
            GlobalContext.setNotificationFrequency(which);
            requestRestart();
            dialog.dismiss();
            setupInterval();
        }
    }
    
    private class OnActionCheckeChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            GlobalContext.setNotificationEnabled(isChecked);
            setupViews();
            requestRestart();
        }
    }
}
