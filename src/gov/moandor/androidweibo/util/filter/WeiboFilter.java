package gov.moandor.androidweibo.util.filter;

import gov.moandor.androidweibo.bean.WeiboStatus;

public interface WeiboFilter {
    public int getId();
    
    public void setId(int id);
    
    public boolean shouldBeRemoved(WeiboStatus status);
}
