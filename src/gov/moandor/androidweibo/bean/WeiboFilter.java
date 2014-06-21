package gov.moandor.androidweibo.bean;

public interface WeiboFilter {
    public int getId();
    
    public void setId(int id);
    
    public boolean shouldBeRemoved(WeiboStatus status);
}
