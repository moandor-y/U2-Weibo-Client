package gov.moandor.androidweibo.util.filter;

import java.io.Serializable;

abstract class BaseWeiboFilter implements WeiboFilter, Serializable {
    private static final long serialVersionUID = 1L;

    private int mId = -1;

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public void setId(int id) {
        mId = id;
    }
}
