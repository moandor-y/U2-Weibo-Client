package gov.moandor.androidweibo.bean;

public interface WeiboFilter {
	public int id;
	
	public boolean shouldBeRemoved(WeiboStatus status);
}
