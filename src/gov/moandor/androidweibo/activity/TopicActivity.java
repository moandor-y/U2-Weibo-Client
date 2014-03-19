package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.fragment.TopicWeiboListFragment;
import gov.moandor.androidweibo.util.PullToRefreshAttacherOwner;

public class TopicActivity extends AbsSwipeBackActivity implements PullToRefreshAttacherOwner {
    private TopicWeiboListFragment mFragment;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragment = (TopicWeiboListFragment) fragmentManager.findFragmentById(R.id.content);
        String topicUri = getIntent().getData().toString();
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(topicUri.substring(topicUri.indexOf("#")));
        int start = topicUri.indexOf("#") + 1;
        int end = topicUri.lastIndexOf("#");
        String topic = topicUri.substring(start, end);
        if (mFragment == null) {
            mFragment = new TopicWeiboListFragment();
            Bundle args = new Bundle();
            args.putString(TopicWeiboListFragment.TOPIC, topic);
            mFragment.setArguments(args);
            fragmentManager.beginTransaction().add(R.id.content, mFragment).commit();
            fragmentManager.executePendingTransactions();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_topic, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.refresh:
            mFragment.refresh();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }
}
