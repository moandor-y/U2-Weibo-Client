package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Account implements Parcelable {
    public long id;
    public String name;
    public String token;
    public String avatarURL;
    public WeiboUser user;
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Account) {
            return id == ((Account) o).id;
        }
        return super.equals(o);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(token);
        dest.writeString(avatarURL);
        dest.writeParcelable(user, flags);
    }
    
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            Account result = new Account();
            result.id = source.readLong();
            result.name = source.readString();
            result.token = source.readString();
            result.avatarURL = source.readString();
            result.user = source.readParcelable(WeiboUser.class.getClassLoader());
            return result;
        }
        
        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
