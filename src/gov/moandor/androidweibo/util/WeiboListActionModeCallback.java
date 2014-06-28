package gov.moandor.androidweibo.util;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;
import gov.moandor.androidweibo.fragment.ConfirmDeleteDialogFragment;

public class WeiboListActionModeCallback implements ActionMode.Callback {
    private static final String DELETE_DIALOG = "delete_dialog";

    private AbsTimelineFragment<WeiboStatus, ?> mFragment;
    private AbsTimelineListAdapter<WeiboStatus> mAdapter;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mFragment.setPullToRefreshEnabled(false);
        mode.getMenuInflater().inflate(R.menu.weibo_long_click, menu);
        if (mAdapter.getSelectedItem().weiboUser.id != GlobalContext.getCurrentAccount().user.id) {
            menu.removeItem(R.id.delete);
        }
        MenuItem shareItem = menu.findItem(R.id.share);
        Utilities.registerShareActionMenu(shareItem, mAdapter.getSelectedItem());
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        WeiboStatus status = mAdapter.getSelectedItem();
        mode.setTitle(status.weiboUser.name);
        if (status.favorited) {
            menu.findItem(R.id.favorite).setVisible(false);
        } else {
            menu.findItem(R.id.unfavorite).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comment:
                comment(mAdapter.getSelectedItem());
                break;
            case R.id.repost:
                repost(mAdapter.getSelectedItem());
                break;
            case R.id.favorite:
                new FavoriteTask(mAdapter.getSelectedItem(), new OnFavoriteFinishedListener(mAdapter.getSelection()))
                        .execute();
                break;
            case R.id.unfavorite:
                new UnfavoriteTask(mAdapter.getSelectedItem(), new OnUnfavoriteFinishedListener(mAdapter.getSelection()))
                        .execute();
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

    public void setFragment(AbsTimelineFragment<WeiboStatus, ?> fragment) {
        mFragment = fragment;
    }

    public void setAdapter(AbsTimelineListAdapter<WeiboStatus> adapter) {
        mAdapter = adapter;
    }

    private void comment(WeiboStatus status) {
        mFragment.startActivity(ActivityUtils.writeCommentActivity(status));
    }

    private void repost(WeiboStatus status) {
        mFragment.startActivity(ActivityUtils.writeWeiboActivity(status));
    }

    private void delete() {
        DeleteWeiboTask task =
                new DeleteWeiboTask(mAdapter.getSelectedItem().id,
                        new OnDeleteFinishedListener(mAdapter.getSelection()));
        ConfirmDeleteDialogFragment dialog = new ConfirmDeleteDialogFragment();
        dialog.setTask(task);
        dialog.show(mFragment.getFragmentManager(), DELETE_DIALOG);
    }

    protected void removeFromDatabase(int position, long accountId, int group) {
    }

    protected void updateDatabase(WeiboStatus status, int position, long accountId, int group) {
    }

    private class OnFavoriteFinishedListener implements FavoriteTask.OnFavoriteFinishedListener {
        private int mSelection;

        public OnFavoriteFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onFavoriteFinished(final WeiboStatus status) {
            mAdapter.updatePosition(mSelection, status);
            mAdapter.notifyDataSetChanged();
            Utilities.notice(R.string.favorited_successfully);
            long accountId = GlobalContext.getCurrentAccount().user.id;
            int group = ConfigManager.getWeiboGroup(accountId);
            updateDatabase(status, mSelection, accountId, group);
        }

        @Override
        public void onFavoriteFailed(WeiboException e) {
            Utilities.notice(R.string.favorite_failed_reason, e.getMessage());
        }
    }

    private class OnUnfavoriteFinishedListener implements UnfavoriteTask.OnUnfavoriteFinishedListener {
        private int mSelection;

        public OnUnfavoriteFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onUnfavoriteFinished(final WeiboStatus status) {
            mAdapter.updatePosition(mSelection, status);
            mAdapter.notifyDataSetChanged();
            Utilities.notice(R.string.unfavorited_successfully);
            long accountId = GlobalContext.getCurrentAccount().user.id;
            int group = ConfigManager.getWeiboGroup(accountId);
            updateDatabase(status, mSelection, accountId, group);
        }

        @Override
        public void onUnfavoriteFailed(WeiboException e) {
            Utilities.notice(R.string.unfavorite_failed_reason, e.getMessage());
        }
    }

    private class OnDeleteFinishedListener implements DeleteWeiboTask.OnDeleteFinishedListener {
        private int mSelection;

        public OnDeleteFinishedListener(int selection) {
            mSelection = selection;
        }

        @Override
        public void onDeleteFinished() {
            mAdapter.removeItem(mSelection);
            mAdapter.notifyDataSetChanged();
            long accountId = GlobalContext.getCurrentAccount().user.id;
            int group = ConfigManager.getWeiboGroup(accountId);
            removeFromDatabase(mSelection, accountId, group);
        }

        @Override
        public void onDeleteFailed(WeiboException e) {
            Utilities.notice(R.string.delete_failed_reason, e.getMessage());
        }
    }
}
