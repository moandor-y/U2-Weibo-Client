package gov.moandor.androidweibo.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.adapter.ImageViewerPagerAdapter;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.concurrency.ImageDownloader.ImageType;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.ZoomOutPageTransformer;

public class ImageViewerActivity extends AbsActivity {
    public static final String IMAGE_TYPE = Utilities.buildIntentExtraName("IMAGE_TYPE");
    public static final String POSITION = Utilities.buildIntentExtraName("POSITION");
    public static final String WEIBO_STATUS = Utilities.buildIntentExtraName("WEIBO_STATUS");
    
    private TextView mCountView;
    private ViewPager mPager;
    private ImageViewerPagerAdapter mPagerAdapter;
    private ImageDownloader.ImageType mType;
    private WeiboStatus mStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        if (savedInstanceState != null) {
            mType = (ImageDownloader.ImageType) savedInstanceState.getSerializable(IMAGE_TYPE);
        } else {
            mType = (ImageType) getIntent().getSerializableExtra(IMAGE_TYPE);
        }
        mStatus = getIntent().getParcelableExtra(WEIBO_STATUS);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ImageViewerPagerAdapter(mType, getUrls(), this);
        mPager.setAdapter(mPagerAdapter);
        int position = getIntent().getIntExtra(POSITION, 0);
        mPager.setCurrentItem(position);
        mCountView = (TextView) findViewById(R.id.count);
        OnPageChangeListener listener = new OnPageChangeListener();
        mPager.setOnPageChangeListener(listener);
        listener.onPageSelected(position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.view_pictures);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(IMAGE_TYPE, mType);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image_viewer, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mType == ImageDownloader.ImageType.PICTURE_LARGE) {
            menu.findItem(R.id.show_ori).setVisible(false);
        } else {
            menu.findItem(R.id.show_ori).setVisible(true);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.save:
            savePicture();
            return true;
        case R.id.show_ori:
            mType = ImageDownloader.ImageType.PICTURE_LARGE;
            mPagerAdapter.setImageType(mType, getUrls());
            mPagerAdapter.notifyDataSetChanged();
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void savePicture() {
        final String url = getUrls()[mPager.getCurrentItem()];
        MyAsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.savePicture(url, mType)) {
                    Utilities.notice(R.string.saved_successfully);
                } else {
                    Utilities.notice(R.string.save_failed);
                }
            }
        });
    }
    
    private String[] getUrls() {
        switch (mType) {
        case PICTURE_SMALL:
            return mStatus.thumbnailPic;
        case PICTURE_MEDIUM:
            return mStatus.bmiddlePic;
        case PICTURE_LARGE:
        default:
            return mStatus.originalPic;
        }
    }
    
    private class OnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int state) {}
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        
        @Override
        public void onPageSelected(int position) {
            String[] urls = getUrls();
            int count = urls.length;
            if (count > 1) {
                mCountView.setText(String.format("%d/%d", position + 1, urls.length));
            }
        }
    }
}
