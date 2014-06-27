package gov.moandor.androidweibo.util.filter;
import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.GlobalContext;

public class SourceFilter extends AbsWeiboTextFilter {
	@Override
    public boolean shouldBeRemoved(WeiboStatus status) {
        if (status.retweetStatus != null && mCheckReposted) {
            return matches(status.source) && matches(status.retweetStatus.source);
        }
        return matches(status.source);
    }
	
    @Override
    protected String getType() {
        return GlobalContext.getInstance().getString(R.string.source);
    }
}
