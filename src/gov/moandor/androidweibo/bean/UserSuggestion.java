package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSuggestion implements Parcelable {
    public long id;
    public String nickname;
    public String remark;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nickname);
        dest.writeString(remark);
    }
    
    public static final Parcelable.Creator<UserSuggestion> CREATOR = new Parcelable.Creator<UserSuggestion>() {
        @Override
        public UserSuggestion createFromParcel(Parcel source) {
            UserSuggestion result = new UserSuggestion();
            result.id = source.readLong();
            result.nickname = source.readString();
            result.remark = source.readString();
            return result;
        }
        
        @Override
        public UserSuggestion[] newArray(int size) {
            return new UserSuggestion[size];
        }
    };
}
