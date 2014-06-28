package gov.moandor.androidweibo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpParams {
    private Map<String, String> mParams = new HashMap<String, String>();

    public void put(String key, String value) {
        mParams.put(key, value);
    }

    public void put(String key, int value) {
        mParams.put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        mParams.put(key, String.valueOf(value));
    }

    public void put(String key, double value) {
        mParams.put(key, String.valueOf(value));
    }

    public void clear() {
        mParams.clear();
    }

    Set<String> keySet() {
        return mParams.keySet();
    }

    String getParams() {
        return Utilities.encodeUrl(mParams);
    }

    String getParam(String key) {
        return mParams.get(key);
    }
}
