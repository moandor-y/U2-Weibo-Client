package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

public abstract class AbsItemBean implements Parcelable {
    public String createdAt;
    public long id;
    public long mid;
    public String text;
    public String source;
    public WeiboUser weiboUser;

    public transient SpannableString textSpannable;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(createdAt);
        dest.writeLong(id);
        dest.writeLong(mid);
        dest.writeString(text);
        dest.writeString(source);
        dest.writeParcelable(weiboUser, flags);
    }

    protected static abstract class ParcelableCreator<T extends AbsItemBean> implements Parcelable.Creator<T> {
        @Override
        public T createFromParcel(Parcel source) {
            T result = onCreateObject();
            result.createdAt = source.readString();
            result.id = source.readLong();
            result.mid = source.readLong();
            result.text = source.readString();
            result.source = source.readString();
            result.weiboUser = source.readParcelable(WeiboUser.class.getClassLoader());
            return result;
        }

        protected abstract T onCreateObject();
    }
}
