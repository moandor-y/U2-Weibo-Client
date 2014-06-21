package gov.moandor.androidweibo.bean;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;

abstract class AbsWeiboFilter implements WeiboFilter {
    private int mId;
    protected boolean mCheckReposted;
    protected boolean mIsRegex;
    protected String mPattern;
    
    @Override
    public int getId() {
        return mId;
    }
    
    @Override
    public void setId(int id) {
        mId = id;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType());
        if (mIsRegex) {
            sb.append("(");
            sb.append(GlobalContext.getInstance().getString(R.string.regex));
            sb.append(")");
        }
        sb.append(": \"");
        sb.append(mPattern);
        sb.append("\"");
        return sb.toString();
    }
    
    protected boolean matches(String text) {
        if (mIsRegex) {
            return text.matches(mPattern);
        } else {
            return text.contains(mPattern);
        }
    }
    
    protected abstract String getType();
}
