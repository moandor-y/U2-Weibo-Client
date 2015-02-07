package gov.moandor.androidweibo.dao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.JsonUtils;
import gov.moandor.androidweibo.util.WeiboException;

public abstract class BaseWeiboStatusTimelineDao extends BaseTimelineJsonDao<WeiboStatus> {
    private Set<Long> mAdIds = new HashSet<>();

    @Override
    protected List<WeiboStatus> parceJson(String jsonStr) throws WeiboException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray ads = json.optJSONArray("ad");
            if (ads != null) {
                for (int i = 0; i < ads.length(); i++) {
                    mAdIds.add(ads.getJSONObject(i).getLong("id"));
                }
            }
        } catch (JSONException e) {
            throw new WeiboException(GlobalContext.getInstance().getString(R.string.json_error));
        }
        return JsonUtils.getWeiboStatusesFromJson(jsonStr);
    }

    protected Set<Long> getAdIds() {
        checkDataFetched();
        return new HashSet<>(mAdIds);
    }
}
