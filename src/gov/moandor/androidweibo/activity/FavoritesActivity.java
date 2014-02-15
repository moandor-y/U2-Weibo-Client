package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.FavoritesFragment;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;

public class FavoritesActivity extends AbsSwipeBackActivity implements PullToRefreshAttacherOwner {
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private FavoritesFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (FavoritesFragment) fragmentManager.findFragmentById(R.id.content);
        if (mFragment == null) {
            mFragment = new FavoritesFragment();
            fragmentManager.beginTransaction().add(R.id.content, mFragment).commit();
            fragmentManager.executePendingTransactions();
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.favorites);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }
}
