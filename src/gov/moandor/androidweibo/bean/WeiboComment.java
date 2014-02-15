package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

public class WeiboComment extends AbsItemBean implements Parcelable {
    public WeiboStatus weiboStatus;
    public WeiboComment repliedComment;
    public transient SpannableString repliedTextSpannable;
    
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
        dest.writeParcelable(weiboStatus, flags);
        dest.writeParcelable(repliedComment, flags);
    }
    
    public static final Parcelable.Creator<WeiboComment> CREATOR = new Parcelable.Creator<WeiboComment>() {
        @Override
        public WeiboComment createFromParcel(Parcel source) {
            WeiboComment result = new WeiboComment();
            result.createdAt = source.readString();
            result.id = source.readLong();
            result.mid = source.readLong();
            result.text = source.readString();
            result.source = source.readString();
            result.weiboUser = source.readParcelable(WeiboUser.class.getClassLoader());
            result.weiboStatus = source.readParcelable(WeiboStatus.class.getClassLoader());
            result.repliedComment = source.readParcelable(WeiboComment.class.getClassLoader());
            return result;
        }
        
        @Override
        public WeiboComment[] newArray(int size) {
            return new WeiboComment[size];
        }
    };
}
