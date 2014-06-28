package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class GpsLocation implements Parcelable {
    public double latitude;
    public double longitude;
    public static final Parcelable.Creator<GpsLocation> CREATOR = new Parcelable.Creator<GpsLocation>() {
        @Override
        public GpsLocation createFromParcel(Parcel source) {
            GpsLocation result = new GpsLocation();
            result.latitude = source.readDouble();
            result.longitude = source.readDouble();
            return result;
        }

        @Override
        public GpsLocation[] newArray(int size) {
            return new GpsLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
