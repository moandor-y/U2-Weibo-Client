package gov.moandor.androidweibo.util;

import android.content.res.Resources;

import gov.moandor.androidweibo.R;

public class WeiboException extends Exception {
    private static final long serialVersionUID = 1L;
    public String mOriError;
    private String mError;
    private int mCode;

    public WeiboException(String error) {
        mError = error;
    }

    public WeiboException(int code) {
        mCode = code;
    }

    public static String getError(int errorCode, String error) {
        try {
            return GlobalContext.getInstance().getString(
                    GlobalContext.getInstance().getResources().getIdentifier("error_code_" + errorCode, "string",
                            GlobalContext.getInstance().getPackageName())
            );
        } catch (Resources.NotFoundException e) {
            if (!TextUtils.isEmpty(error)) {
                return error;
            } else {
                return GlobalContext.getInstance().getString(R.string.unknown_error);
            }
        }
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
}
