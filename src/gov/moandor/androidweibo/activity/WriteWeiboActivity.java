package gov.moandor.androidweibo.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.GpsLocation;
import gov.moandor.androidweibo.bean.WeiboDraft;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.fragment.AddPictureDialogFragment;
import gov.moandor.androidweibo.notification.SendWeiboService;
import gov.moandor.androidweibo.util.CheatSheet;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.ImageUtils;
import gov.moandor.androidweibo.util.TextUtils;
import gov.moandor.androidweibo.util.Utilities;

public class WriteWeiboActivity extends AbsWriteActivity {
    public static final String RETWEET_WEIBO_STATUS = Utilities.buildIntentExtraName("RETWEET_WEIBO_STATUS");
    public static final String DRAFT = Utilities.buildIntentExtraName("DRAFT");
    public static final String ADD_PIC_DIALOG = "add_pic_dialog";
    public static final String STATE_PIC_PATH = "state_pic_path";
    public static final String STATE_COMMENT_WHEN_REPOST = "state_comment_when_repost";
    public static final String STATE_COMMENT_ORI_WHEN_REPOST = "state_comment_ori_when_repost";
    public static final int CAMERA_REQUEST_CODE = 0;
    public static final int GALLERY_REQUEST_CODE = 1;
    
    private boolean mCommentWhenRepost;
    private boolean mCommentOriWhenRepost;
    private WeiboStatus mRetweetWeiboStatus;
    private Uri mImageFileUri;
    private String mPicPath;
    private GpsLocation mLocation;
    private ImageButton mAddPicButton;
    private ImageView mPreview;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreview = (ImageView) findViewById(R.id.preview);
        WeiboDraft draft = getIntent().getParcelableExtra(DRAFT);
        if (draft != null) {
            mRetweetWeiboStatus = draft.retweetStatus;
            mPicPath = draft.picPath;
            if (!TextUtils.isEmpty(draft.content)) {
                mEditText.setText(draft.content);
                mEditText.setSelection(draft.content.length());
            }
            if (!TextUtils.isEmpty(draft.error)) {
                mEditText.setError(draft.error);
            }
        } else {
            mRetweetWeiboStatus = getIntent().getParcelableExtra(RETWEET_WEIBO_STATUS);
        }
        if (mRetweetWeiboStatus != null) {
            mEditText.setHint(mRetweetWeiboStatus.text);
            if (mRetweetWeiboStatus.retweetStatus != null) {
                mEditText.setText("//@" + mRetweetWeiboStatus.weiboUser.name + ":" + mRetweetWeiboStatus.text);
                mEditText.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mEditText.getViewTreeObserver().removeOnPreDrawListener(this);
                        mEditText.setSelection(0);
                        return true;
                    }
                });
            }
        }
        if (mRetweetWeiboStatus == null) {
            getSupportActionBar().setTitle(R.string.write_weibo);
        } else {
            getSupportActionBar().setTitle(R.string.repost_weibo);
        }
        if (savedInstanceState != null) {
            mCommentWhenRepost = savedInstanceState.getBoolean(STATE_COMMENT_WHEN_REPOST);
            mCommentOriWhenRepost = savedInstanceState.getBoolean(STATE_COMMENT_ORI_WHEN_REPOST);
        }
        loadPicPreview();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
        case CAMERA_REQUEST_CODE:
            if (TextUtils.isEmpty(mEditText.getText())) {
                mEditText.setText(R.string.share_picture);
                mEditText.setSelection(mEditText.getText().length());
            }
            mPicPath = getPath(mImageFileUri);
            loadPicPreview();
            break;
        case GALLERY_REQUEST_CODE:
            if (TextUtils.isEmpty(mEditText.getText())) {
                mEditText.setText(R.string.share_picture);
                mEditText.setSelection(mEditText.getText().length());
            }
            mPicPath = getPath(data.getData());
            loadPicPreview();
            break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_write_weibo, menu);
        menu.findItem(R.id.comment_when_repost).setChecked(mCommentWhenRepost);
        menu.findItem(R.id.comment_ori_when_repost).setChecked(mCommentOriWhenRepost);
        if (mRetweetWeiboStatus == null) {
            menu.removeItem(R.id.comment_when_repost);
            menu.removeItem(R.id.comment_ori_when_repost);
        } else {
            menu.removeItem(R.id.insert_topic);
            menu.removeItem(R.id.add_location);
            if (mRetweetWeiboStatus.retweetStatus == null) {
                menu.removeItem(R.id.comment_ori_when_repost);
            }
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add_location:
            addLocation();
            return true;
        case R.id.insert_topic:
            insertTopic();
            return true;
        case R.id.comment_when_repost:
            if (item.isChecked()) {
                item.setChecked(false);
                mCommentWhenRepost = false;
            } else {
                item.setChecked(true);
                mCommentWhenRepost = true;
            }
            return true;
        case R.id.comment_ori_when_repost:
            if (item.isChecked()) {
                item.setChecked(false);
                mCommentOriWhenRepost = false;
            } else {
                item.setChecked(true);
                mCommentOriWhenRepost = true;
            }
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PIC_PATH, mPicPath);
        outState.putBoolean(STATE_COMMENT_WHEN_REPOST, mCommentWhenRepost);
        outState.putBoolean(STATE_COMMENT_ORI_WHEN_REPOST, mCommentOriWhenRepost);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mPicPath = savedInstanceState.getString(STATE_PIC_PATH);
        loadPicPreview();
    }
    
    @Override
    void onSend(String content) {
        Intent intent = new Intent();
        intent.setClass(GlobalContext.getInstance(), SendWeiboService.class);
        intent.putExtra(SendWeiboService.TOKEN, GlobalContext.getCurrentAccount().token);
        intent.putExtra(SendWeiboService.WEIBO_DRAFT, onCreateDraft(content));
        startService(intent);
        finish();
    }
    
    @Override
    void onCreateBottomMenu(ViewGroup container) {
        if (mRetweetWeiboStatus == null) {
            getLayoutInflater().inflate(R.layout.bottom_menu_write_weibo, container);
            mAddPicButton = (ImageButton) container.findViewById(R.id.add_picture);
            CheatSheet.setup(mAddPicButton, R.string.add_picture);
        } else {
            getLayoutInflater().inflate(R.layout.bottom_menu_write_no_pic, container);
            CheatSheet.setup(container.findViewById(R.id.insert_topic), R.string.insert_topic);
        }
        CheatSheet.setup(container.findViewById(R.id.at), R.string.mention);
        CheatSheet.setup(container.findViewById(R.id.emotion), R.string.insert_emotion);
    }
    
    @Override
    void onBottomMenuItemSelected(View view) {
        switch (view.getId()) {
        case R.id.add_picture:
            addPicture();
            break;
        case R.id.at:
            atUser();
            break;
        case R.id.emotion:
            toggleEmotionPanel();
            break;
        case R.id.insert_topic:
            insertTopic();
            break;
        }
    }
    
    private void addPicture() {
        AddPictureDialogFragment dialog = new AddPictureDialogFragment();
        dialog.setOnClickListener(mOnAddPicDialogClickListener);
        dialog.show(getSupportFragmentManager(), ADD_PIC_DIALOG);
    }
    
    private String getPath(Uri uri) {
        String path = uri.getPath();
        if (path.startsWith("/external")) {
            String[] proj = {MediaColumns.DATA};
            Cursor cursor = new CursorLoader(this, uri, proj, null, null, null).loadInBackground();
            int columnIndex = cursor.getColumnIndex(MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        }
        return path;
    }
    
    @Override
    WeiboDraft onCreateDraft(String content) {
        WeiboDraft draft = new WeiboDraft();
        draft.content = content;
        draft.accountId = GlobalContext.getCurrentAccount().user.id;
        draft.picPath = mPicPath;
        draft.retweetStatus = mRetweetWeiboStatus;
        draft.location = mLocation;
        draft.commentWhenRepost = mCommentWhenRepost;
        draft.commentOriWhenRepost = mCommentOriWhenRepost;
        return draft;
    }
    
    private void addLocation() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Utilities.notice(R.string.open_gps);
            return;
        }
        Utilities.notice(R.string.updatind_location);
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 10, mLocationListener);
        } else if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, mLocationListener);
        }
    }
    
    private void onLocationUpdated(Location location) {
        Utilities.notice(R.string.location_updated);
        mLocation = new GpsLocation();
        mLocation.latitude = location.getLatitude();
        mLocation.longitude = location.getLongitude();
    }
    
    private void loadPicPreview() {
        if (!TextUtils.isEmpty(mPicPath)) {
            Bitmap thumb = ImageUtils.getBitmapFromFile(mPicPath, Utilities.dpToPx(30), Utilities.dpToPx(30));
            mAddPicButton.setImageBitmap(thumb);
            Bitmap preview =
                    ImageUtils.getBitmapFromFile(mPicPath, Utilities.getScreenWidth(), Utilities.getScreenHeight());
            mPreview.setImageBitmap(preview);
            mPreview.setVisibility(View.VISIBLE);
        } else {
            mAddPicButton.setImageResource(R.drawable.ic_camera_dark);
            mPreview.setVisibility(View.GONE);
            mPreview.setImageBitmap(null);
        }
    }
    
    private OnAddPicDialogClickListener mOnAddPicDialogClickListener = new OnAddPicDialogClickListener();
    
    private class OnAddPicDialogClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case 0:
                mImageFileUri =
                        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                if (mImageFileUri != null) {
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageFileUri);
                    if (Utilities.isIntentAvailable(intent)) {
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    } else {
                        Utilities.notice(R.string.no_camera_app);
                    }
                } else {
                    Utilities.notice(R.string.cannot_add_picture);
                }
                break;
            case 1:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
                break;
            }
        }
    }
    
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        
        @Override
        public void onProviderEnabled(String provider) {}
        
        @Override
        public void onProviderDisabled(String provider) {}
        
        @Override
        public void onLocationChanged(Location location) {
            ((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
            onLocationUpdated(location);
        }
    };
}
