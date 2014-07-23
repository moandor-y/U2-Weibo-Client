package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseWeiboStatusTimelineDao extends BaseTimelineJsonDao<WeiboStatus> {
    @Override
    protected List<WeiboStatus> parceJson(String json) throws WeiboException {
        return JsonUtils.getWeiboStatusesFromJson(json);
    }
}
