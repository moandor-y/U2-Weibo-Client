package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseDataSetJsonDao<T> {
    protected String mUrl = getUrl();
    
    public abstract T[] getData() throws WeiboException;
    
    protected abstract String getUrl();
}
