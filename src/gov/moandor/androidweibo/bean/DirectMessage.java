package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectMessage extends AbsItemBean {
    public WeiboUser recipient;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(recipient, flags);
    }
    
    public static final Parcelable.Creator<DirectMessage> CREATOR = new ParcelableCreator<DirectMessage>() {
        @Override
        public DirectMessage createFromParcel(Parcel source) {
            DirectMessage result = super.createFromParcel(source);
            result.recipient = source.readParcelable(WeiboUser.class.getClassLoader());
            return result;
        }
        
        @Override
        public DirectMessage[] newArray(int size) {
            return new DirectMessage[size];
        }
        
        @Override
        protected DirectMessage onCreateObject() {
            return new DirectMessage();
        }
    };
}
