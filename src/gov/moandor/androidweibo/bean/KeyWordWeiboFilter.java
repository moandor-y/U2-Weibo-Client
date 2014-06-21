package gov.moandor.androidweibo.bean;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.util.GlobalContext;

public class KeyWordWeiboFilter extends AbsWeiboFilter {
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
