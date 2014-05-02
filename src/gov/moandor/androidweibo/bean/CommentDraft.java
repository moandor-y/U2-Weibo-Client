package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentDraft extends AbsDraftBean {
    public WeiboStatus commentedStatus;
    public WeiboComment repliedComment;
    public boolean commentOri;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(commentedStatus, 0);
        dest.writeParcelable(repliedComment, 0);
        dest.writeBooleanArray(new boolean[]{commentOri});
    }
    
    public static final Parcelable.Creator<CommentDraft> CREATOR = new ParcelableCreator<CommentDraft>() {
        @Override
        public CommentDraft createFromParcel(Parcel source) {
            CommentDraft result = super.createFromParcel(source);
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
        
        @Override
        protected CommentDraft onCreateObject() {
            return new CommentDraft();
        }
    };
}
