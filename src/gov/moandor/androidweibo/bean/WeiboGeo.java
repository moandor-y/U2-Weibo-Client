package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboGeo implements Parcelable {
    public String cityName;
    public String provinceName;
    public String address;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeString(provinceName);
        dest.writeString(address);
    }
    
    public static final Parcelable.Creator<WeiboGeo> CREATOR = new Parcelable.Creator<WeiboGeo>() {
        @Override
        public WeiboGeo createFromParcel(Parcel source) {
            WeiboGeo result = new WeiboGeo();
            result.cityName = source.readString();
            result.provinceName = source.readString();
            result.address = source.readString();
            return result;
        }
        
        @Override
        public WeiboGeo[] newArray(int size) {
            return new WeiboGeo[size];
        }
    };
}
