package gov.moandor.androidweibo.util.filter;
import android.os.Parcelable;
import android.os.Parcel;
import java.io.Serializable;

abstract class BaseWeiboFilter implements WeiboFilter, Serializable {
	private int mId = -1;
	
	@Override
    public int getId() {
        return mId;
    }
	
    @Override
    public void setId(int id) {
        mId = id;
    }
}
