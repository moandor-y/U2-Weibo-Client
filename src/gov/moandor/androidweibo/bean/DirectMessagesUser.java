package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectMessagesUser extends AbsItemBean {
    public DirectMessage message;
    public int unreadCount;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(message, flags);
        dest.writeInt(unreadCount);
    }
    
    public static final Parcelable.Creator<DirectMessagesUser> CREATOR = new ParcelableCreator<DirectMessagesUser>() {
        @Override
        public DirectMessagesUser createFromParcel(Parcel source) {
            DirectMessagesUser result = super.createFromParcel(source);
            result.message = source.readParcelable(DirectMessage.class.getClassLoader());
            result.unreadCount = source.readInt();
            return result;
        }
        
        @Override
        public DirectMessagesUser[] newArray(int size) {
            return new DirectMessagesUser[size];
        }
        
        @Override
        protected DirectMessagesUser onCreateObject() {
            return new DirectMessagesUser();
        }
    };
}
