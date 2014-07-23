package gov.moandor.androidweibo.util;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;
import gov.moandor.androidweibo.fragment.ConfirmDeleteDialogFragment;

public class CommentListActionModeCallback implements ActionMode.Callback {
    private static final String DELETE_DIALOG = "delete_dialog";

    private AbsTimelineFragment<WeiboComment, ?> mFragment;
    private AbsTimelineListAdapter<WeiboComment> mAdapter;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mFragment.setPullToRefreshEnabled(false);
        mode.getMenuInflater().inflate(R.menu.long_click_comment, menu);
        if (mAdapter.getSelectedItem().weiboUser.id != GlobalContext.getCurrentAccount().user.id) {
            menu.removeItem(R.id.delete);
        }
        MenuItem shareItem = menu.findItem(R.id.share);
        Utilities.registerShareActionMenu(shareItem, mAdapter.getSelectedItem());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        WeiboComment comment = mAdapter.getSelectedItem();
        mode.setTitle(comment.weiboUser.name);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply:
                reply();
                break;
            case R.id.view_weibo:
                mFragment.startActivity(ActivityUtils.weiboActivity(mAdapter.getSelectedItem().weiboStatus));
                break;
            case R.id.view_user:
                mFragment.startActivity(ActivityUtils.userActivity(mAdapter.getSelectedItem().weiboUser));
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.copy:
                Utilities.copyText(mAdapter.getSelectedItem().text);
                break;
            default:
                return true;
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFragment.onActionModeFinished();
        mAdapter.setSelectedPosition(-1);
        mAdapter.notifyDataSetChanged();
        mFragment.setPullToRefreshEnabled(true);
    }

    private void reply() {
        WeiboComment comment = mAdapter.getSelectedItem();
        mFragment.startActivity(ActivityUtils.writeCommentActivity(comment.weiboStatus, comment));
    }

    private void delete() {
        DeleteCommentTask task =
                new DeleteCommentTask(mAdapter.getSelectedItem().id, new OnDeleteFinishedListener(mAdapter
                        .getSelection()));
        ConfirmDeleteDialogFragment dialog = new ConfirmDeleteDialogFragment();
        dialog.setTask(task);
        dialog.show(mFragment.getFragmentManager(), DELETE_DIALOG);
    }

    public void setAdapter(AbsTimelineListAdapter<WeiboComment> adapter) {
        mAdapter = adapter;
    }

    public void setFragment(AbsTimelineFragment<WeiboComment, ?> fragment) {
        mFragment = fragment;
    }

    protected void removeFromDatabase(int position, long accountId, int group) {
    }

    private class OnDeleteFinishedListener implements DeleteCommentTask.OnDeleteFinishedListener {
        private int mSelection;

        public OnDeleteFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onDeleteFinished() {
            mAdapter.removeItem(mSelection);
            mAdapter.notifyDataSetChanged();
            long accountId = GlobalContext.getCurrentAccount().user.id;
            int group = ConfigManager.getCommentFilter();
            removeFromDatabase(mSelection, accountId, group);
        }

        @Override
        public void onDeleteFailed(WeiboException e) {
            Utilities.notice(R.string.delete_failed_reason, e.getMessage());
        }
    }
}
