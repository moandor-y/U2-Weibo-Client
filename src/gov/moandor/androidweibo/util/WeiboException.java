package gov.moandor.androidweibo.util;

import android.content.res.Resources;

import gov.moandor.androidweibo.R;

public class WeiboException extends Exception {
    private static final long serialVersionUID = 1L;
    private String mError;
    private int mCode;
    public String mOriError;
    
    public WeiboException(String error) {
        mError = error;
    }
    
    public WeiboException(int code) {
        mCode = code;
    }
    
    @Override
    public String getMessage() {
        if (!TextUtils.isEmpty(mError)) {
            return mError;
        } else {
            return getError(mCode, mOriError);
        }
    }
    
    public int getCode() {
        return mCode;
    }
    
    public static String getError(int errorCode, String error) {
        try {
            return GlobalContext.getInstance().getString(
                    GlobalContext.getInstance().getResources().getIdentifier("error_code_" + errorCode, "string",
                            GlobalContext.getInstance().getPackageName()));
        } catch (Resources.NotFoundException e) {
            if (!TextUtils.isEmpty(error)) {
                return error;
            } else {
                return GlobalContext.getInstance().getString(R.string.unknown_error);
            }
        }
    }
}
