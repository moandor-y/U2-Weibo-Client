package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboGroup implements Parcelable {
    public long id;
    public String name;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }
    
    public static final Parcelable.Creator<WeiboGroup> CREATOR = new Parcelable.Creator<WeiboGroup>() {
        @Override
        public WeiboGroup createFromParcel(Parcel source) {
            WeiboGroup result = new WeiboGroup();
            result.id = source.readLong();
            result.name = source.readString();
            return result;
        }
        
        @Override
        public WeiboGroup[] newArray(int size) {
            return new WeiboGroup[size];
        }
    };
}
