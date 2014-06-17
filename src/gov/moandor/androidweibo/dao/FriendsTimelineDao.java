package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.Iterator;
import java.util.List;
import gov.moandor.androidweibo.bean.WeiboFilter;

public class FriendsTimelineDao extends BaseWeiboStatusTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_FRIENDS_TIMELINE;
    }
    
    @Override
    public List<WeiboStatus> execute() throws WeiboException {
        List<WeiboStatus> result = super.execute();
        if (Utilities.isBmEnabled() && ConfigManager.isIgnoringUnfollowedEnabled()) {
            long[] followingIds = DatabaseUtils.getFollowingIds(GlobalContext.getCurrentAccount().user.id);
            if (followingIds != null) {
                Iterator<WeiboStatus> iterator = result.iterator();
                while (iterator.hasNext()) {
                    WeiboStatus status = iterator.next();
                    WeiboUser user = status.weiboUser;
                    if (user != null && !contains(followingIds, user.id)) {
                        iterator.remove();
                    }
                }
            }
        }
		WeiboFilter[] filters = DatabaseUtils.getWeiboFilters();
		Iterator<WeiboStatus> iterator = result.iterator();
		while (iterator.hasNext()) {
			WeiboStatus status = iterator.next();
			for (WeiboFilter filter : filters) {
				if (filter.shouldBeRemoved(status)) {
					iterator.remove();
					break;
				}
			}
		}
        return result;
    }
    
    private static boolean contains(long[] array, long value) {
        for (long element : array) {
            if (value == element) {
                return true;
            }
        }
        return false;
    }
}
