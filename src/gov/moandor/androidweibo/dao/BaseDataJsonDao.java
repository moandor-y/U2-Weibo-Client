package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseDataJsonDao<T> {
    protected String mUrl = getUrl();
    
    public abstract T fetchData() throws WeiboException;
    
    protected abstract String getUrl();
}
