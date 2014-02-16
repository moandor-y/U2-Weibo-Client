package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Account implements Parcelable {
    public String token;
    public WeiboUser user;
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Account) {
            return user.id == ((Account) o).user.id;
        }
        return super.equals(o);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
        dest.writeParcelable(user, flags);
    }
    
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            Account result = new Account();
            result.token = source.readString();
            result.user = source.readParcelable(WeiboUser.class.getClassLoader());
            return result;
        }
        
        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
