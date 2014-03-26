package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class WeiboStatusDao extends BaseTimelineJsonDao<WeiboStatus> {
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return Utilities.getWeiboStatusesFromJson(json);
    }
}
