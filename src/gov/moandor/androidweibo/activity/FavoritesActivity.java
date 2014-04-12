package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.FavoritesFragment;

public class FavoritesActivity extends AbsActivity {
    private FavoritesFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
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
}
