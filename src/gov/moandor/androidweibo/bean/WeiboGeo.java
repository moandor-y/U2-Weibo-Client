package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboGeo implements Parcelable {
    public double[] coordinate = new double[2];
    public static final Parcelable.Creator<WeiboGeo> CREATOR = new Parcelable.Creator<WeiboGeo>() {
        @Override
        public WeiboGeo createFromParcel(Parcel source) {
            WeiboGeo result = new WeiboGeo();
            source.readDoubleArray(result.coordinate);
            return result;
        }

        @Override
        public WeiboGeo[] newArray(int size) {
            return new WeiboGeo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDoubleArray(coordinate);
    }
}
