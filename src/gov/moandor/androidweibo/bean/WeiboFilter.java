package gov.moandor.androidweibo.bean;

public interface WeiboFilter {
    public int getId();
    
    public int setId(int id);
    
    public boolean shouldBeRemoved(WeiboStatus status);
}
