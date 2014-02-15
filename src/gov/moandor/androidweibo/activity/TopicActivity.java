package gov.moandor.androidweibo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

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
        if (mFragment == null) {
            String topic = getIntent().getData().toString();
            int start = topic.indexOf("#") + 1;
            int end = topic.lastIndexOf("#");
            topic = topic.substring(start, end);
            mFragment = new TopicWeiboListFragment();
            Bundle args = new Bundle();
            args.putString(TopicWeiboListFragment.TOPIC, topic);
            mFragment.setArguments(args);
            fragmentManager.beginTransaction().add(R.id.content, mFragment).commit();
            fragmentManager.executePendingTransactions();
        }
    }
    
    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }
}
