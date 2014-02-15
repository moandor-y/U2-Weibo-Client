package gov.moandor.androidweibo.bean;

import android.os.Parcelable;

public abstract class AbsDraftBean implements Parcelable {
    public String content;
    public long accountId;
    public transient int id;
    public String error;
}
