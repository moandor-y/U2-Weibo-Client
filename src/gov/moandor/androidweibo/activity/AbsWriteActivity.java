package gov.moandor.androidweibo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.AbsDraftBean;
import gov.moandor.androidweibo.bean.UserSuggestion;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.widget.SmileyPicker;

public abstract class AbsWriteActivity extends AbsActivity {
    private static final int MAX_LENGTH = 140;
    private static final int REQUEST_AT_USER = 2;
    private static final long SMILEY_PICKER_DELAY = 100;
    private static final String SAVE_DRAFT_DIALOG = "save_draft_dialog";
    public static final String AT_USER_RESULT_NAME = Utilities.buildIntentExtraName("AT_USER_RESULT_NAME");
    
    EditText mEditText;
    private SmileyPicker mSmileyPicker;
    private Button mSendButton;
    private MyAsyncTask<Void, Void, Void> mSaveDraftTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mSmileyPicker = (SmileyPicker) findViewById(R.id.smiley_picker);
        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(new OnSendClickListener());
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker();
            }
        });
        mEditText.addTextChangedListener(new LengthTextWatcher());
        mSmileyPicker.setOnItemClickListener(new OnSmileyClickListener());
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayout bottomMenu = (LinearLayout) findViewById(R.id.bottom_menu);
        bottomMenu.removeAllViews();
        onCreateBottomMenu(bottomMenu);
        OnBottomMenuItemClickListener listener = new OnBottomMenuItemClickListener();
        int count = bottomMenu.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = bottomMenu.getChildAt(i);
            view.setOnClickListener(listener);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        switch (requestCode) {
        case REQUEST_AT_USER:
            UserSuggestion suggestion = data.getParcelableExtra(AT_USER_RESULT_NAME);
            int index = mEditText.getSelectionStart();
            String toInsert = "@" + suggestion.nickname + " ";
            StringBuilder stringBuilder = new StringBuilder(mEditText.getText());
            stringBuilder.insert(index, toInsert);
            mEditText.setText(stringBuilder.toString());
            mEditText.setSelection(index + toInsert.length());
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (!TextUtils.isEmpty(mEditText.getText())) {
                new SaveDraftDialogFragment().show(getSupportFragmentManager(), SAVE_DRAFT_DIALOG);
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (mSmileyPicker.getVisibility() == View.VISIBLE) {
            mSmileyPicker.setVisibility(View.GONE);
        } else if (!TextUtils.isEmpty(mEditText.getText())) {
            new SaveDraftDialogFragment().show(getSupportFragmentManager(), SAVE_DRAFT_DIALOG);
        } else {
            finish();
        }
    }
    
    private void hideSmileyPicker() {
        Utilities.showKeyboard(mEditText);
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSmileyPicker.setVisibility(View.GONE);
            }
        }, SMILEY_PICKER_DELAY);
    }
    
    private void showSmileyPicker() {
        Utilities.hideKeyboard(mEditText.getWindowToken());
        GlobalContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSmileyPicker.setVisibility(View.VISIBLE);
            }
        }, SMILEY_PICKER_DELAY);
    }
    
    void toggleEmotionPanel() {
        if (mSmileyPicker.getVisibility() == View.VISIBLE) {
            hideSmileyPicker();
        } else {
            showSmileyPicker();
        }
    }
    
    private void saveDraft() {
        if (mSaveDraftTask != null && mSaveDraftTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            return;
        }
        final AbsDraftBean draft = onCreateDraft(mEditText.getText().toString());
        mSaveDraftTask = new MyAsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatabaseUtils.insertDraft(draft);
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {
                finish();
            }
        };
        mSaveDraftTask.execute();
    }
    
    void insertTopic() {
        int index = mEditText.getSelectionStart();
        StringBuilder stringBuilder = new StringBuilder(mEditText.getText());
        stringBuilder.insert(index, "##");
        mEditText.setText(stringBuilder.toString());
        mEditText.setSelection(index + 1);
    }
    
    void atUser() {
        startActivityForResult(ActivityUtils.atUserActivity(), REQUEST_AT_USER);
    }
    
    private class OnSmileyClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String key = SmileyPicker.getKey(position);
            String old = mEditText.getText().toString();
            int index = mEditText.getSelectionStart();
            StringBuilder stringBuilder = new StringBuilder(old);
            stringBuilder.insert(index, key);
            mEditText.setText(stringBuilder.toString());
            mEditText.setSelection(index + key.length());
        }
    }
    
    private class OnSendClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String content = mEditText.getText().toString();
            if (TextUtils.isEmpty(content)) {
                mEditText.setError(getString(R.string.cannot_be_empty));
                return;
            } else if (Utilities.sendLength(content) >= MAX_LENGTH) {
                mEditText.setError(getString(R.string.too_many_words));
                return;
            }
            onSend(content);
            Utilities.hideKeyboard(mEditText.getWindowToken());
        }
    }
    
    private class LengthTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int len = Utilities.sendLength(mEditText.getText().toString());
            if (len == 0) {
                mSendButton.setTextColor(getResources().getColor(R.color.action_menu_text_color));
                mSendButton.setText(R.string.send);
            } else {
                int left = MAX_LENGTH - len;
                if (left < 0) {
                    mSendButton.setTextColor(Color.RED);
                } else {
                    mSendButton.setTextColor(getResources().getColor(R.color.action_menu_text_color));
                }
                mSendButton.setText(String.valueOf(left));
            }
        }
        
        @Override
        public void afterTextChanged(Editable s) {}
    }
    
    private class OnBottomMenuItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onBottomMenuItemSelected(v);
        }
    }
    
    public static class SaveDraftDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.save_draft);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((AbsWriteActivity) getActivity()).saveDraft();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            return builder.create();
        }
    }
    
    abstract void onSend(String content);
    
    abstract void onCreateBottomMenu(ViewGroup container);
    
    abstract void onBottomMenuItemSelected(View view);
    
    abstract AbsDraftBean onCreateDraft(String content);
}
