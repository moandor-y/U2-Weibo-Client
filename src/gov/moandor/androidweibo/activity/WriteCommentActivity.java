package gov.moandor.androidweibo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.CommentDraft;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.notification.SendCommentService;
import gov.moandor.androidweibo.util.CheatSheet;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public class WriteCommentActivity extends AbsWriteActivity {
    public static final String COMMENTED_WEIBO_STATUS;
    public static final String REPLIED_WEIBO_COMMENT;
    public static final String DRAFT;
    public static final String STATE_COMMENT_ORI = "state_comment_ori";
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        COMMENTED_WEIBO_STATUS = packageName + ".COMMENTED_WEIBO_STATUS";
        REPLIED_WEIBO_COMMENT = packageName + ".REPLIED_WEIBO_COMMENT";
        DRAFT = packageName + ".DRAFT";
    }
    
    private boolean mCommentOri;
    private WeiboStatus mCommentedStatus;
    private WeiboComment mRepliedComment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommentDraft draft = getIntent().getParcelableExtra(DRAFT);
        if (draft != null) {
            mCommentedStatus = draft.commentedStatus;
            mRepliedComment = draft.repliedComment;
            if (!TextUtils.isEmpty(draft.content)) {
                mEditText.setText(draft.content);
                mEditText.setSelection(draft.content.length());
            }
            if (!TextUtils.isEmpty(draft.error)) {
                mEditText.setError(draft.error);
            }
        } else {
            mCommentedStatus = getIntent().getParcelableExtra(COMMENTED_WEIBO_STATUS);
            mRepliedComment = getIntent().getParcelableExtra(REPLIED_WEIBO_COMMENT);
        }
        if (mRepliedComment != null) {
            mEditText.setHint(mRepliedComment.text);
        } else if (mCommentedStatus != null) {
            mEditText.setHint(mCommentedStatus.text);
        }
        if (mRepliedComment == null) {
            getSupportActionBar().setTitle(R.string.write_comment);
        } else {
            getSupportActionBar().setTitle(R.string.reply_to_comment);
        }
        if (savedInstanceState != null) {
            mCommentOri = savedInstanceState.getBoolean(STATE_COMMENT_ORI);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCommentedStatus.retweetStatus != null) {
            getMenuInflater().inflate(R.menu.activity_write_comment, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.comment_ori:
            if (item.isChecked()) {
                item.setChecked(false);
                mCommentOri = false;
            } else {
                item.setChecked(true);
                mCommentOri = true;
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_COMMENT_ORI, mCommentOri);
    }
    
    @Override
    void onSend(String content) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), SendCommentService.class);
        intent.putExtra(SendCommentService.TOKEN, GlobalContext.getCurrentAccount().token);
        intent.putExtra(SendCommentService.COMMENT_DRAFT, onCreateDraft(content));
        startService(intent);
        finish();
    }
    
    @Override
    void onCreateBottomMenu(ViewGroup container) {
        getLayoutInflater().inflate(R.layout.bottom_menu_write_no_pic, container);
        CheatSheet.setup(container.findViewById(R.id.insert_topic), R.string.insert_topic);
        CheatSheet.setup(container.findViewById(R.id.at), R.string.mention);
        CheatSheet.setup(container.findViewById(R.id.emotion), R.string.insert_emotion);
    }
    
    @Override
    void onBottomMenuItemSelected(View view) {
        switch (view.getId()) {
        case R.id.insert_topic:
            insertTopic();
            break;
        case R.id.at:
            atUser();
            break;
        case R.id.emotion:
            toggleEmotionPanel();
            break;
        }
    }
    
    @Override
    CommentDraft onCreateDraft(String content) {
        CommentDraft draft = new CommentDraft();
        draft.content = content;
        draft.accountId = GlobalContext.getCurrentAccount().id;
        draft.commentedStatus = mCommentedStatus;
        draft.repliedComment = mRepliedComment;
        draft.commentOri = mCommentOri;
        return draft;
    }
}
