package gov.moandor.androidweibo.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

public class WeiboComment extends AbsItemBean {
    public WeiboStatus weiboStatus;
    public WeiboComment repliedComment;
    public transient SpannableString repliedTextSpannable;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(weiboStatus, flags);
        dest.writeParcelable(repliedComment, flags);
    }
    
    public static final Parcelable.Creator<WeiboComment> CREATOR = new ParcelableCreator<WeiboComment>() {
        @Override
        public WeiboComment createFromParcel(Parcel source) {
            WeiboComment result = super.createFromParcel(source);
            result.weiboStatus = source.readParcelable(WeiboStatus.class.getClassLoader());
            result.repliedComment = source.readParcelable(WeiboComment.class.getClassLoader());
            return result;
        }
        
        @Override
        public WeiboComment[] newArray(int size) {
            return new WeiboComment[size];
        }
        
        @Override
        protected WeiboComment onCreateObject() {
            return new WeiboComment();
        }
    };
}
