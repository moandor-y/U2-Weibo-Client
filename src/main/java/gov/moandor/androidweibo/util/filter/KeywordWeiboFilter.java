package gov.moandor.androidweibo.util.filter;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.GlobalContext;

public class KeywordWeiboFilter extends AbsWeiboTextFilter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean shouldBeRemoved(WeiboStatus status) {
        if (status.retweetStatus != null && mCheckReposted) {
            return matches(status.text) && matches(status.retweetStatus.text);
        }
        return matches(status.text);
    }

    @Override
    protected String getType() {
        return GlobalContext.getInstance().getString(R.string.keywords);
    }
}
