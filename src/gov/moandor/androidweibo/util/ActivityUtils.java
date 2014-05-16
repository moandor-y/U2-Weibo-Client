package gov.moandor.androidweibo.util;
import android.content.Intent;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.activity.WeiboActivity;
import gov.moandor.androidweibo.activity.MainActivity;

public class ActivityUtils {
	public static Intent weiboActivity(WeiboStatus status) {
		Intent intent = new Intent();
		intent.setClass(GlobalContext.getInstance(), getWeiboActivityClass());
		intent.putExtra(WeiboActivity.WEIBO_STATUS, status);
		return intent;
	}
	
	private static Class<?> getWeiboActivityClass() {
		if (ConfigManager.isSwipeBackEnabled()) {
			return WeiboActivity.Translucent.class;
		} else {
			return WeiboActivity.class;
		}
	}
}
