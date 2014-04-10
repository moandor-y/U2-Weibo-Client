package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.bean.WeiboUser;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public class FriendsTimelineDao extends BaseWeiboStatusTimelineDao {
    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_FRIENDS_TIMELINE;
    }
    
    @Override
    public List<WeiboStatus> execute() throws WeiboException {
        List<WeiboStatus> result = super.execute();
        if (Utilities.isBmEnabled()) {
            long[] followingIds = DatabaseUtils.getFollowingIds(GlobalContext.getCurrentAccount().user.id);
            if (followingIds != null) {
                for (WeiboStatus status : result) {
                    WeiboUser user = status.weiboUser;
                    if (user != null && !contains(followingIds, user.id)) {
                        result.remove(status);
                    }
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
