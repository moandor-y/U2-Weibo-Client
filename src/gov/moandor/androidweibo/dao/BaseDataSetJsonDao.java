package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class BaseDataSetJsonDao<T> {
    protected String mUrl = getUrl();
    
    public abstract List<T> fetchData() throws WeiboException;
    
    protected abstract String getUrl();
}
