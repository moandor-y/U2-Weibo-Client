package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UnreadCount implements Parcelable {
    public int weiboStatus;
    public int comment;
    public int mentionWeibo;
    public int mentionComment;
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(weiboStatus);
        dest.writeInt(comment);
        dest.writeInt(mentionWeibo);
        dest.writeInt(mentionComment);
    }
    
    public static final Parcelable.Creator<UnreadCount> CREATOR = new Parcelable.Creator<UnreadCount>() {
        @Override
        public UnreadCount createFromParcel(Parcel source) {
            UnreadCount result = new UnreadCount();
            result.weiboStatus = source.readInt();
            result.comment = source.readInt();
            result.mentionWeibo = source.readInt();
            result.mentionComment = source.readInt();
            return result;
        }
        
        @Override
        public UnreadCount[] newArray(int size) {
            return new UnreadCount[size];
        }
    };
}
