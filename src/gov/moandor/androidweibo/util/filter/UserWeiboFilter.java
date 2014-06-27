package gov.moandor.androidweibo.util.filter;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.TextUtils;

public class UserWeiboFilter extends BaseWeiboFilter {
	private WeiboUser mUser;
	private String mUserName;
	private long mUserId;
	
	@Override
	public boolean shouldBeRemoved(WeiboStatus status) {
		WeiboUser user = status.weiboUser;
		if (user == null) {
			return false;
		}
		if (mUser != null) {
			return user.id == mUser.id;
		} else if (!TextUtils.isEmpty(mUserName)) {
			return user.name != null && user.name.equals(mUserName);
		} else {
			return user.id == mUserId;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(GlobalContext.getInstance().getString(R.string.user));
		sb.append(":");
		if (mUser != null) {
			sb.append(mUser.name);
		} else if (!TextUtils.isEmpty(mUserName)) {
			sb.append(mUserName);
		} else {
			sb.append(mUserId);
		}
		return sb.toString();
	}
	
	public void setUser(WeiboUser user) {
		mUser = user;
	}
	
	public WeiboUser getUser() {
		return mUser;
	}
	
	public void setUserId(long id) {
		mUserId = id;
	}
	
	public long getUserId() {
		return mUserId;
	}
	
	public void setUserName(String name) {
		mUserName = name;
	}
	
	public String getUserName() {
		return mUserName;
	}
}
