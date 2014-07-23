package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class AbsDraftBean implements Parcelable {
    public String content;
    public long accountId;
    public transient int id;
    public String error;

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
    }

    protected static abstract class ParcelableCreator<T extends AbsDraftBean> implements Parcelable.Creator<T> {
        @Override
        public T createFromParcel(Parcel source) {
            T result = onCreateObject();
            result.content = source.readString();
            result.accountId = source.readLong();
            result.id = source.readInt();
            result.error = source.readString();
            return result;
        }

        protected abstract T onCreateObject();
    }
}
