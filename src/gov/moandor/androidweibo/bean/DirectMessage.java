package gov.moandor.androidweibo.bean;

public class DirectMessage {
    public long id;
    public String createdAt;
    public String text;
    public WeiboUser sender;
    public WeiboUser recipient;
}
