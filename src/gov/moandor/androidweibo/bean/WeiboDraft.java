package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboDraft extends AbsDraftBean {
    public WeiboStatus retweetStatus;
    public String picPath;
    public GpsLocation location;
    public boolean commentWhenRepost;
    public boolean commentOriWhenRepost;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeLong(accountId);
        dest.writeInt(id);
        dest.writeString(error);
        dest.writeParcelable(retweetStatus, 0);
        dest.writeString(picPath);
        dest.writeParcelable(location, 0);
        boolean[] bools = {commentWhenRepost, commentOriWhenRepost};
        dest.writeBooleanArray(bools);
    }
    
    public static final Parcelable.Creator<WeiboDraft> CREATOR = new Parcelable.Creator<WeiboDraft>() {
        @Override
        public WeiboDraft createFromParcel(Parcel source) {
            WeiboDraft result = new WeiboDraft();
            result.content = source.readString();
            result.accountId = source.readLong();
            result.id = source.readInt();
            result.error = source.readString();
            result.retweetStatus = source.readParcelable(WeiboStatus.class.getClassLoader());
            result.picPath = source.readString();
            result.location = source.readParcelable(GpsLocation.class.getClassLoader());
            boolean[] bools = new boolean[2];
            source.readBooleanArray(bools);
            result.commentWhenRepost = bools[0];
            result.commentOriWhenRepost = bools[1];
            return result;
        }
        
        @Override
        public WeiboDraft[] newArray(int size) {
            return new WeiboDraft[size];
        }
    };
}
