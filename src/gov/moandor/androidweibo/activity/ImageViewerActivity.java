package gov.moandor.androidweibo.activity;

import android.app.Activity;
import android.content.Intent;
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
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.concurrency.ImageDownloader.ImageType;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.ZoomOutPageTransformer;

public class ImageViewerActivity extends AbsSwipeBackActivity {
    public static final String URLS;
    public static final String IMAGE_TYPE;
    public static final String POSITION;
    
    static {
        String packageName = GlobalContext.getInstance().getPackageName();
        URLS = packageName + ".urls";
        IMAGE_TYPE = packageName + ".image.type";
        POSITION = packageName + ".position";
    }
    
    private TextView mCountView;
    private ViewPager mPager;
    private String[] mUrls;
    private ImageDownloader.ImageType mType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        mUrls = getIntent().getStringArrayExtra(URLS);
        mType = (ImageType) getIntent().getSerializableExtra(IMAGE_TYPE);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ImageViewerPagerAdapter(mType, mUrls));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image_viewer, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void savePicture() {
        final String url = mUrls[mPager.getCurrentItem()];
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
    
    public static void start(WeiboStatus status, int position, Activity activity) {
        String[] urls;
        ImageDownloader.ImageType type = Utilities.getDetailPictureType();
        switch (type) {
        case PICTURE_SMALL:
            urls = status.thumbnailPic;
            break;
        case PICTURE_MEDIUM:
            urls = status.bmiddlePic;
            break;
        case PICTURE_LARGE:
        default:
            urls = status.originalPic;
            break;
        }
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.URLS, urls);
        intent.putExtra(ImageViewerActivity.IMAGE_TYPE, type);
        intent.putExtra(ImageViewerActivity.POSITION, position);
        activity.startActivity(intent);
    }
    
    private class OnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int state) {}
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        
        @Override
        public void onPageSelected(int position) {
            int count = mUrls.length;
            if (count > 1) {
                mCountView.setText(String.format("%d/%d", position + 1, mUrls.length));
            }
        }
    }
}
