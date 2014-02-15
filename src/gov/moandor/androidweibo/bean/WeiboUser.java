package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class WeiboUser implements Parcelable {
    public long id;
    public String name;
    public String location;
    public String description;
    public String profileImageUrl;
    public String gender;
    public int followersCount;
    public int friendsCount;
    public int statusesCount;
    public boolean following;
    public boolean allowAllActMsg;
    public boolean verified;
    public String remark;
    public boolean allowAllComment;
    public String avatarLargeUrl;
    public String verifiedReason;
    public boolean followMe;
    public int onlineStatus;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(description);
        dest.writeString(profileImageUrl);
        dest.writeString(gender);
        dest.writeInt(followersCount);
        dest.writeInt(friendsCount);
        dest.writeInt(statusesCount);
        dest.writeString(remark);
        dest.writeString(avatarLargeUrl);
        dest.writeString(verifiedReason);
        dest.writeInt(onlineStatus);
        dest.writeBooleanArray(new boolean[]{following, allowAllActMsg, verified, allowAllComment, followMe});
    }
    
    public static final Parcelable.Creator<WeiboUser> CREATOR = new Parcelable.Creator<WeiboUser>() {
        @Override
        public WeiboUser createFromParcel(Parcel source) {
            WeiboUser result = new WeiboUser();
            result.id = source.readLong();
            result.name = source.readString();
            result.location = source.readString();
            result.description = source.readString();
            result.profileImageUrl = source.readString();
            result.gender = source.readString();
            result.followersCount = source.readInt();
            result.friendsCount = source.readInt();
            result.statusesCount = source.readInt();
            result.remark = source.readString();
            result.avatarLargeUrl = source.readString();
            result.verifiedReason = source.readString();
            result.onlineStatus = source.readInt();
            boolean[] bools = new boolean[5];
            source.readBooleanArray(bools);
            result.following = bools[0];
            result.allowAllActMsg = bools[1];
            result.verified = bools[2];
            result.allowAllComment = bools[3];
            result.followMe = bools[4];
            return result;
        }
        
        @Override
        public WeiboUser[] newArray(int size) {
            return new WeiboUser[size];
        }
    };
}
