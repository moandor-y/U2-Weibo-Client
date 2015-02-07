package gov.moandor.androidweibo.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.ConfigManager;
import gov.moandor.androidweibo.util.DatabaseUtils;
import gov.moandor.androidweibo.util.UrlHelper;
import gov.moandor.androidweibo.util.WeiboException;
import gov.moandor.androidweibo.util.filter.WeiboFilter;

public class FriendsTimelineDao extends BaseWeiboStatusTimelineDao {

    @Override
    protected String getUrl() {
        return UrlHelper.STATUSES_FRIENDS_TIMELINE;
    }

    @Override
    public List<WeiboStatus> execute() throws WeiboException {
        List<WeiboStatus> result = super.execute();
        if (ConfigManager.isBmEnabled() && ConfigManager.isIgnoringUnfollowedEnabled()) {
            Set<Long> adIds = getAdIds();
            Iterator<WeiboStatus> iterator = result.iterator();
            while (iterator.hasNext()) {
                WeiboStatus status = iterator.next();
                if (adIds.contains(Long.valueOf(status.id))) {
                    iterator.remove();
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
}
