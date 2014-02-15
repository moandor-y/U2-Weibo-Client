package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentDraft extends AbsDraftBean {
    public WeiboStatus commentedStatus;
    public WeiboComment repliedComment;
    public boolean commentOri;
    
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
        dest.writeParcelable(commentedStatus, 0);
        dest.writeParcelable(repliedComment, 0);
        dest.writeBooleanArray(new boolean[]{commentOri});
    }
    
    public static final Parcelable.Creator<CommentDraft> CREATOR = new Parcelable.Creator<CommentDraft>() {
        @Override
        public CommentDraft createFromParcel(Parcel source) {
            CommentDraft result = new CommentDraft();
            result.content = source.readString();
            result.accountId = source.readLong();
            result.id = source.readInt();
            result.error = source.readString();
            result.commentedStatus = source.readParcelable(WeiboStatus.class.getClassLoader());
            result.repliedComment = source.readParcelable(WeiboComment.class.getClassLoader());
            boolean[] bools = new boolean[1];
            source.readBooleanArray(bools);
            result.commentOri = bools[0];
            return result;
        }
        
        @Override
        public CommentDraft[] newArray(int size) {
            return new CommentDraft[size];
        }
    };
}
