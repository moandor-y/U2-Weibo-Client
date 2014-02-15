package gov.moandor.androidweibo.bean;

import android.os.Parcelable;
import android.text.SpannableString;

public abstract class AbsItemBean implements Parcelable {
    public String createdAt;
    public long id;
    public long mid;
    public String text;
    public String source;
    public WeiboUser weiboUser;
    
    public transient SpannableString textSpannable;
}
