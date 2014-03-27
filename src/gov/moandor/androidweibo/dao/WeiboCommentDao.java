package gov.moandor.androidweibo.dao;

import gov.moandor.androidweibo.bean.WeiboComment;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

import java.util.List;

public abstract class WeiboCommentDao extends BaseTimelineJsonDao<WeiboComment> {
    @Override
    protected List<WeiboComment> parceJson(String json) throws WeiboException {
        return JsonUtils.getWeiboCommentsFromJson(json);
    }
}
