package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseHttpDao<T> {
    protected String mUrl = getUrl();
    
    public abstract T execute() throws WeiboException;
    
    protected abstract String getUrl();
}
