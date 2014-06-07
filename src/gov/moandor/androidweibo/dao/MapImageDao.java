package gov.moandor.androidweibo.dao;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.ImageDownloadTaskCache;
import gov.moandor.androidweibo.concurrency.ImageDownloader;
import gov.moandor.androidweibo.util.FileUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.HttpParams;
import gov.moandor.androidweibo.util.HttpUtils;
import gov.moandor.androidweibo.util.ImageUtils;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.WeiboException;

public class MapImageDao extends BaseHttpDao<Bitmap> {
    private String mToken;
    private double mLatitude;
    private double mLongitude;
    
    @Override
    public Bitmap execute() throws WeiboException {
        HttpParams params = new HttpParams();
        params.putParam("access_token", mToken);
        params.putParam("center_coordinate", mLongitude + "," + mLatitude);
        params.putParam("size", "600x380");
        params.putParam("zoom", 14);
        HttpUtils.Method method = HttpUtils.Method.GET;
        String response = HttpUtils.executeNormalTask(method, mUrl, params);
        try {
            JSONObject json = new JSONObject(response);
            JSONArray array = json.getJSONArray("map");
            json = array.getJSONObject(0);
            String mapUrl = json.getString("image_url");
            ImageDownloader.ImageType type = ImageDownloader.ImageType.PICTURE_LARGE;
            if (ImageDownloadTaskCache.waitForPictureDownload(mapUrl, null, type)) {
                String path = FileUtils.getImagePathFromUrl(mapUrl, type);
                return ImageUtils.getBitmapFromFile(path, -1, -1);
            }
        } catch (JSONException e) {
            Logger.logExcpetion(e);
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
        return null;
    }
    
    @Override
    protected String getUrl() {
        return UrlHelper.LOCATION_BASE_GET_MAP_IMAGE;
    }
    
    public void setToken(String token) {
        mToken = token;
    }
    
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
    
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
