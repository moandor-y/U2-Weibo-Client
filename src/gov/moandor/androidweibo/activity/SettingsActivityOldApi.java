package gov.moandor.androidweibo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;

public class SettingsActivityOldApi extends AbsActivity {
    private static final String THEME_DIALOG = "theme_dialog";
    private static final String AVATAR_MODE_DIALOG = "avatar_mode_dialog";
    private static final String PICTURE_MODE_DIALOG = "picture_mode_dialog";
    private static final String PICTURE_WIFI_MODE_DIALOG = "picture_wifi_mode_dialog";
    private static final String FONT_SIZE_DIALOG = "font_size_dialog";
    private static final String LOAD_WEIBO_COUNT_DIALOG = "load_weibo_count_dialog";
    private static final String COMMENT_REPOST_LIST_AVATAR_MODE_DIALOG = "comment_repost_list_avatar_mode_dialog";
    
    private static boolean sNeedRestart;
    private TextView mThemeStatus;
    private TextView mFontSizeStatus;
    private TextView mLoadWeiboCountStatus;
    private TextView mAvatarModeStatus;
    private TextView mPictureModeStatus;
    private TextView mPictureWifiModeStatus;
    private TextView mCommentRepostListAvatarStatus;
    private TextView mAvatarModeLabel;
    private TextView mPictureModeLabel;
    private TextView mPictureWifiModeLabel;
    private TextView mCommentRepostListAvatarLabel;
    private CheckedTextView mFastScroll;
    private CheckedTextView mNoPictureMode;
    private CheckedTextView mWifiAutoDownloadPic;
    private CheckedTextView mListHwAccel;
    private CheckedTextView mPicHwAccel;
    private View mAvatarModeLayout;
    private View mPictureModeLayout;
    private View mPictureWifiModeLayout;
    private View mCommentRepostListAvatarLayout;
    private String[] mThemes;
    private String[] mAvatarModes;
    private String[] mPictureModes;
    private String[] mFontSizes;
    private String[] mCommentRepostListAvatarModes;
    private String[] mLoadWeiboCounts;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Resources res = getResources();
        mThemes = res.getStringArray(R.array.themes);
        mAvatarModes = res.getStringArray(R.array.avatar_modes);
        mPictureModes = res.getStringArray(R.array.picture_modes);
        mFontSizes = res.getStringArray(R.array.font_sizes);
        mCommentRepostListAvatarModes = res.getStringArray(R.array.comment_repost_list_avatar_modes);
        mLoadWeiboCounts = res.getStringArray(R.array.load_weibo_counts);
        mThemeStatus = (TextView) findViewById(R.id.theme_status);
        mFontSizeStatus = (TextView) findViewById(R.id.font_size_status);
        mFastScroll = (CheckedTextView) findViewById(R.id.fast_scroll);
        mLoadWeiboCountStatus = (TextView) findViewById(R.id.load_weibo_count_status);
        mNoPictureMode = (CheckedTextView) findViewById(R.id.no_picture_mode);
        mAvatarModeStatus = (TextView) findViewById(R.id.avatar_mode_status);
        mPictureModeStatus = (TextView) findViewById(R.id.picture_mode_status);
        mPictureWifiModeStatus = (TextView) findViewById(R.id.picture_wifi_mode_status);
        mCommentRepostListAvatarStatus = (TextView) findViewById(R.id.comment_repost_list_avatar_mode_status);
        mAvatarModeLabel = (TextView) findViewById(R.id.avatar_mode_label);
        mPictureModeLabel = (TextView) findViewById(R.id.picture_mode_label);
        mPictureWifiModeLabel = (TextView) findViewById(R.id.picture_wifi_mode_label);
        mCommentRepostListAvatarLabel = (TextView) findViewById(R.id.comment_repost_list_avatar_mode_label);
        mWifiAutoDownloadPic = (CheckedTextView) findViewById(R.id.wifi_auto_download_pic);
        mListHwAccel = (CheckedTextView) findViewById(R.id.list_hw_accel);
        mPicHwAccel = (CheckedTextView) findViewById(R.id.pic_hw_accel);
        mAvatarModeLayout = findViewById(R.id.avatar_mode);
        mPictureModeLayout = findViewById(R.id.picture_mode);
        mPictureWifiModeLayout = findViewById(R.id.picture_wifi_mode);
        mCommentRepostListAvatarLayout = findViewById(R.id.comment_repost_list_avatar_mode);
        mFastScroll.setOnClickListener(new OnFastScrollClickListener());
        mNoPictureMode.setOnClickListener(new OnNoPictureModeClickListener());
        mAvatarModeLayout.setOnClickListener(new OnAvatarModeClickListener());
        mPictureModeLayout.setOnClickListener(new OnPictureModeClickListener());
        mPictureWifiModeLayout.setOnClickListener(new OnPictureWifiModeClickListener());
        mCommentRepostListAvatarLayout.setOnClickListener(new OnCommentRepostListAvatarModeClickListener());
        mWifiAutoDownloadPic.setOnClickListener(new OnWifiAutoDownloadPicClickListener());
        mListHwAccel.setOnClickListener(new OnListHwAccelClickListener());
        mPicHwAccel.setOnClickListener(new OnPicHwAccelClickListener());
        findViewById(R.id.notification).setOnClickListener(new OnNotificationClickListener());
        findViewById(R.id.theme).setOnClickListener(new OnThemeClickListener());
        findViewById(R.id.font_size).setOnClickListener(new OnFontSizeClickListener());
        findViewById(R.id.load_weibo_count).setOnClickListener(new OnLoadWeiboCountClickListener());
        setupTheme();
        setupFontSize();
        setupFastScroll();
        setupLoadWeiboCount();
        setupNoPictureMode();
        setupAvatarMode();
        setupPictureMode();
        setupPictureWifiMode();
        setupCommentRepostListAvatarMode();
        setupWifiAutoDownloadPic();
        setupListHwAccel();
        setupPicHwAccel();
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (sNeedRestart) {
                sNeedRestart = false;
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (sNeedRestart) {
                sNeedRestart = false;
                Intent intent = new Intent();
                intent.setClass(GlobalContext.getInstance(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void setupTheme() {
        mThemeStatus.setText(mThemes[GlobalContext.getAppTheme()]);
    }
    
    private void setupFontSize() {
        mFontSizeStatus.setText(mFontSizes[GlobalContext.getFontSizeMode()]);
    }
    
    private void setupFastScroll() {
        mFastScroll.setChecked(GlobalContext.isFastScrollEnabled());
    }
    
    private void setupLoadWeiboCount() {
        int count = GlobalContext.getLoadWeiboCountMode();
        if (count != 0) {
            mLoadWeiboCountStatus.setText(String.valueOf(Utilities.getLoadWeiboCount()));
        } else {
            mLoadWeiboCountStatus.setText(R.string.auto);
        }
    }
    
    private void setupNoPictureMode() {
        mNoPictureMode.setChecked(GlobalContext.isNoPictureMode());
    }
    
    private void setupAvatarMode() {
        if (GlobalContext.isNoPictureMode()) {
            mAvatarModeLayout.setEnabled(false);
            mAvatarModeLabel.setEnabled(false);
            mAvatarModeStatus.setEnabled(false);
        } else {
            mAvatarModeLayout.setEnabled(true);
            mAvatarModeLabel.setEnabled(true);
            mAvatarModeStatus.setEnabled(true);
        }
        mAvatarModeStatus.setText(mAvatarModes[GlobalContext.getAvatarQuality()]);
    }
    
    private void setupPictureMode() {
        if (GlobalContext.isNoPictureMode()) {
            mPictureModeLayout.setEnabled(false);
            mPictureModeLabel.setEnabled(false);
            mPictureModeStatus.setEnabled(false);
        } else {
            mPictureModeLayout.setEnabled(true);
            mPictureModeLabel.setEnabled(true);
            mPictureModeStatus.setEnabled(true);
        }
        mPictureModeStatus.setText(mPictureModes[GlobalContext.getPictureQuality()]);
    }
    
    private void setupPictureWifiMode() {
        if (GlobalContext.isNoPictureMode()) {
            mPictureWifiModeLayout.setEnabled(false);
            mPictureWifiModeLabel.setEnabled(false);
            mPictureWifiModeStatus.setEnabled(false);
        } else {
            mPictureWifiModeLayout.setEnabled(true);
            mPictureWifiModeLabel.setEnabled(true);
            mPictureWifiModeStatus.setEnabled(true);
        }
        mPictureWifiModeStatus.setText(mPictureModes[GlobalContext.getPictureWifiQuality()]);
    }
    
    private void setupCommentRepostListAvatarMode() {
        if (GlobalContext.isNoPictureMode()) {
            mCommentRepostListAvatarLayout.setEnabled(false);
            mCommentRepostListAvatarLabel.setEnabled(false);
            mCommentRepostListAvatarStatus.setEnabled(false);
        } else {
            mCommentRepostListAvatarLayout.setEnabled(true);
            mCommentRepostListAvatarLabel.setEnabled(true);
            mCommentRepostListAvatarStatus.setEnabled(true);
        }
        mCommentRepostListAvatarStatus.setText(mCommentRepostListAvatarModes[GlobalContext
                .getCommentRepostListAvatarMode()]);
    }
    
    private void setupWifiAutoDownloadPic() {
        mWifiAutoDownloadPic.setChecked(GlobalContext.isWifiAutoDownloadPicEnabled());
        mWifiAutoDownloadPic.setEnabled(!GlobalContext.isNoPictureMode());
    }
    
    private void setupListHwAccel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListHwAccel.setChecked(GlobalContext.isListHwAccelEnabled());
        } else {
            mListHwAccel.setEnabled(false);
        }
    }
    
    private void setupPicHwAccel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPicHwAccel.setChecked(GlobalContext.isPicHwAccelEnabled());
        } else {
            mPicHwAccel.setEnabled(false);
        }
    }
    
    static AlertDialog.Builder buildListDialog(int titleResId, String[] items, int checkedItem,
            DialogInterface.OnClickListener listener, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setSingleChoiceItems(items, checkedItem, listener);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setTitle(titleResId);
        return builder;
    }
    
    private static void requestRestart() {
        if (!sNeedRestart) {
            sNeedRestart = true;
        }
    }
    
    private class OnNotificationClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), NotificationSettingsActivity.class);
            startActivity(intent);
        }
    }
    
    private class OnThemeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.theme, mThemes, GlobalContext.getAppTheme(),
                            new OnThemeSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), THEME_DIALOG);
        }
    }
    
    private class OnFontSizeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.font_size, mFontSizes, GlobalContext.getFontSizeMode(),
                            new OnFontSizeSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), FONT_SIZE_DIALOG);
        }
    }
    
    private class OnFastScrollClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mFastScroll.toggle();
            GlobalContext.setFastScrollEnabled(mFastScroll.isChecked());
            requestRestart();
        }
    }
    
    private class OnLoadWeiboCountClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.load_weibo_count, mLoadWeiboCounts, GlobalContext.getLoadWeiboCountMode(),
                            new OnLoadWeiboCountSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), LOAD_WEIBO_COUNT_DIALOG);
        }
    }
    
    private class OnNoPictureModeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mNoPictureMode.toggle();
            GlobalContext.setNoPictureMode(mNoPictureMode.isChecked());
            requestRestart();
            setupAvatarMode();
            setupPictureMode();
            setupPictureWifiMode();
            setupCommentRepostListAvatarMode();
            setupWifiAutoDownloadPic();
        }
    }
    
    private class OnAvatarModeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.avatar, mAvatarModes, GlobalContext.getAvatarQuality(),
                            new OnAvatarModeSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), AVATAR_MODE_DIALOG);
        }
    }
    
    private class OnPictureModeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.picture, mPictureModes, GlobalContext.getPictureQuality(),
                            new OnPictureModeSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), PICTURE_MODE_DIALOG);
        }
    }
    
    private class OnPictureWifiModeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.picture_wifi, mPictureModes, GlobalContext.getPictureWifiQuality(),
                            new OnPictureWifiModeSelectedListener(), SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), PICTURE_WIFI_MODE_DIALOG);
        }
    }
    
    private class OnWifiAutoDownloadPicClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mWifiAutoDownloadPic.toggle();
            GlobalContext.setWifiAutoDownloadPicEnabled(mWifiAutoDownloadPic.isChecked());
            requestRestart();
        }
    }
    
    private class OnListHwAccelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mListHwAccel.toggle();
            GlobalContext.setListHwAccelEnabled(mListHwAccel.isChecked());
            requestRestart();
        }
    }
    
    private class OnPicHwAccelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mPicHwAccel.toggle();
            GlobalContext.setPicHwAccelEnabled(mPicHwAccel.isChecked());
            requestRestart();
        }
    }
    
    private class OnCommentRepostListAvatarModeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder =
                    buildListDialog(R.string.comment_repost_list_avatar, mCommentRepostListAvatarModes, GlobalContext
                            .getCommentRepostListAvatarMode(), new OnCommentRepostListAvatarModeSelectedListener(),
                            SettingsActivityOldApi.this);
            SettingsDialogFragment dialog = new SettingsDialogFragment();
            dialog.setBuilder(builder);
            dialog.show(getSupportFragmentManager(), COMMENT_REPOST_LIST_AVATAR_MODE_DIALOG);
        }
    }
    
    private class OnThemeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getAppTheme()) {
                return;
            }
            GlobalContext.setAppTheme(which);
            requestRestart();
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setClass(GlobalContext.getInstance(), SettingsActivityOldApi.class);
            finish();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.stay, R.anim.activity_fade_out);
        }
    }
    
    private class OnFontSizeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getFontSizeMode()) {
                return;
            }
            GlobalContext.setFontSizeMode(which);
            requestRestart();
            dialog.dismiss();
            setupFontSize();
        }
    }
    
    private class OnLoadWeiboCountSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getLoadWeiboCountMode()) {
                return;
            }
            GlobalContext.setLoadWeiboCountMode(which);
            requestRestart();
            dialog.dismiss();
            setupLoadWeiboCount();
        }
    }
    
    private class OnAvatarModeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getAvatarQuality()) {
                return;
            }
            GlobalContext.setAvatarQuality(which);
            requestRestart();
            dialog.dismiss();
            setupAvatarMode();
        }
    }
    
    private class OnPictureModeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getPictureQuality()) {
                return;
            }
            GlobalContext.setPictureQuality(which);
            requestRestart();
            dialog.dismiss();
            setupPictureMode();
        }
    }
    
    private class OnPictureWifiModeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getPictureWifiQuality()) {
                return;
            }
            GlobalContext.setPictureWifiQuality(which);
            requestRestart();
            dialog.dismiss();
            setupPictureWifiMode();
        }
    }
    
    private class OnCommentRepostListAvatarModeSelectedListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == GlobalContext.getCommentRepostListAvatarMode()) {
                return;
            }
            GlobalContext.setCommentRepostListAvatarMode(which);
            dialog.dismiss();
            setupCommentRepostListAvatarMode();
        }
    }
    
    public static class SettingsDialogFragment extends DialogFragment {
        private AlertDialog.Builder mBuilder;
        
        public void setBuilder(AlertDialog.Builder builder) {
            mBuilder = builder;
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mBuilder.create();
        }
    }
}
