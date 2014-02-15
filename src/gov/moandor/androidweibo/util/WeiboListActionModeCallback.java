package gov.moandor.androidweibo.util;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.activity.WriteCommentActivity;
import gov.moandor.androidweibo.activity.WriteWeiboActivity;
import gov.moandor.androidweibo.adapter.AbsTimelineListAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.fragment.AbsTimelineFragment;
import gov.moandor.androidweibo.fragment.ConfirmDeleteDialogFragment;

public class WeiboListActionModeCallback implements ActionMode.Callback {
    private static final String DELETE_DIALOG = "delete_dialog";
    
    private AbsTimelineFragment<WeiboStatus, ?> mFragment;
    private AbsTimelineListAdapter<WeiboStatus> mAdapter;
    private ShareActionProvider mShareActionProvider;
    
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mFragment.setPullToRefreshEnabled(false);
        mode.getMenuInflater().inflate(R.menu.weibo_long_click, menu);
        if (mAdapter.getSelectedItem().weiboUser.id != GlobalContext.getCurrentAccount().id) {
            menu.removeItem(R.id.delete);
        }
        MenuItem shareItem = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        WeiboStatus status = mAdapter.getSelectedItem();
        WeiboUser user = status.weiboUser;
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "@" + user.name + " : " + status.text);
        if (status.picCount > 0) {
            File small = new File(status.thumbnailPic[0]);
            File medium = new File(status.bmiddlePic[0]);
            File large = new File(status.originalPic[0]);
            Uri uri = null;
            if (large.exists()) {
                uri = Uri.fromFile(large);
            } else if (medium.exists()) {
                uri = Uri.fromFile(medium);
            } else if (small.exists()) {
                uri = Uri.fromFile(small);
            }
            if (uri != null) {
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("image/*");
            }
        }
        if (Utilities.isIntentAvailable(intent)) {
            mShareActionProvider.setShareIntent(intent);
        }
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
            new FavoriteTask(mAdapter.getSelectedItem(), new OnFavoriteFinishedListener(
                    mAdapter.getSelection())).execute();
            break;
        case R.id.unfavorite:
            new UnfavoriteTask(mAdapter.getSelectedItem(), new OnUnfavoriteFinishedListener(
                    mAdapter.getSelection())).execute();
            break;
        case R.id.delete:
            delete();
            break;
        case R.id.share:
            return true;
        case R.id.copy:
            Utilities.copyText(mAdapter.getSelectedItem().text);
            break;
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
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteCommentActivity.class);
        intent.putExtra(WriteCommentActivity.COMMENTED_WEIBO_STATUS, status);
        mFragment.startActivity(intent);
    }
    
    private void repost(WeiboStatus status) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), WriteWeiboActivity.class);
        intent.putExtra(WriteWeiboActivity.RETWEET_WEIBO_STATUS, status);
        mFragment.startActivity(intent);
    }
    
    private void delete() {
        DeleteWeiboTask task = new DeleteWeiboTask(mAdapter.getSelectedItem().id, 
                new OnDeleteFinishedListener(mAdapter.getSelection()));
        ConfirmDeleteDialogFragment dialog = new ConfirmDeleteDialogFragment();
        dialog.setTask(task);
        dialog.show(mFragment.getFragmentManager(), DELETE_DIALOG);
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
            long accountId = GlobalContext.getCurrentAccount().id;
            int group = GlobalContext.getWeiboGroup();
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
            long accountId = GlobalContext.getCurrentAccount().id;
            int group = GlobalContext.getWeiboGroup();
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
            long accountId = GlobalContext.getCurrentAccount().id;
            int group = GlobalContext.getWeiboGroup();
            removeFromDatabase(mSelection, accountId, group);
        }
        
        @Override
        public void onDeleteFailed(WeiboException e) {
            Utilities.notice(R.string.delete_failed_reason, e.getMessage());
        }
    }
    
    protected void removeFromDatabase(int position, long accountId, int group) {}
    
    protected void updateDatabase(WeiboStatus status, int position, long accountId, int group) {}
}
