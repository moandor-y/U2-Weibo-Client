package gov.moandor.androidweibo.util.filter;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;

import java.util.regex.PatternSyntaxException;

public abstract class AbsWeiboTextFilter extends BaseWeiboFilter {
    private static final long serialVersionUID = 1L;
    
    protected boolean mCheckReposted;
    protected boolean mIsRegex;
    protected String mPattern;
    
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
    
    public void setPattern(String pattern) {
        mPattern = pattern;
    }
    
    public String getPattern() {
        return mPattern;
    }
    
    public void setCheckReposted(boolean checkReposted) {
        mCheckReposted = checkReposted;
    }
    
    public boolean getCheckReposted() {
        return mCheckReposted;
    }
    
    public void setIsRegex(boolean isRegex) {
        mIsRegex = isRegex;
    }
    
    public boolean isRegex() {
        return mIsRegex;
    }
    
    protected boolean matches(String text) {
        if (mIsRegex) {
            try {
                return text.matches(mPattern);
            } catch (PatternSyntaxException e) {
                Logger.logException(e);
                return false;
            }
        } else {
            return text.contains(mPattern);
        }
    }
    
    protected abstract String getType();
}
