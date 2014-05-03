package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DirectMessagesUser implements Parcelable {
    public WeiboUser weiboUser;
    public DirectMessage message;
    public int unreadCount;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(weiboUser, flags);
        dest.writeParcelable(message, flags);
        dest.writeInt(unreadCount);
    }
    
    public static final Parcelable.Creator<DirectMessagesUser> CREATOR = new Parcelable.Creator<DirectMessagesUser>() {
        @Override
        public DirectMessagesUser createFromParcel(Parcel source) {
            DirectMessagesUser result = new DirectMessagesUser();
            result.weiboUser = source.readParcelable(WeiboUser.class.getClassLoader());
            result.message = source.readParcelable(DirectMessage.class.getClassLoader());
            result.unreadCount = source.readInt();
            return result;
        }
        
        @Override
        public DirectMessagesUser[] newArray(int size) {
            return new DirectMessagesUser[size];
        }
    };
}
