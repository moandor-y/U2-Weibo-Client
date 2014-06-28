package gov.moandor.androidweibo.dao;

import java.util.List;

import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseWeiboCommentTimelineDao extends BaseTimelineJsonDao<WeiboComment> {
    @Override
    protected List<WeiboComment> parceJson(String json) throws WeiboException {
        return JsonUtils.getWeiboCommentsFromJson(json);
    }
}
