package gov.moandor.androidweibo.activity;

import android.os.Bundle;

import gov.moandor.androidweibo.bean.WeiboStatus;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;
import gov.moandor.androidweibo.dao.WeiboMidToIdDao;
import gov.moandor.androidweibo.dao.WeiboStatusDao;
import gov.moandor.androidweibo.util.ActivityUtils;
import gov.moandor.androidweibo.util.GlobalContext;
import gov.moandor.androidweibo.util.Logger;
import gov.moandor.androidweibo.util.Utilities;
import gov.moandor.androidweibo.util.WeiboException;

public class IncomingUrlActivity extends AbsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getData().toString();
        if (Utilities.isWeiboMidUrl(url)) {
            String mid = Utilities.getMidFromUrl(url);
            new RedirectToWeiboActivityTask(mid).execute();
        } else {
            finish();
        }
    }
    
    private class RedirectToWeiboActivityTask extends MyAsyncTask<Void, Void, WeiboStatus> {
        private String mMid;
        private String mToken;
        
        private RedirectToWeiboActivityTask(String mid) {
            mMid = mid;
        }
        
        @Override
        protected void onPreExecute() {
            mToken = GlobalContext.getCurrentAccount().token;
        }
        
        @Override
        protected WeiboStatus doInBackground(Void... params) {
            WeiboMidToIdDao dao = new WeiboMidToIdDao();
            dao.setToken(mToken);
            dao.setMid(mMid);
            try {
                long id = dao.execute().longValue();
                WeiboStatusDao statusDao = new WeiboStatusDao();
                statusDao.setToken(mToken);
                statusDao.setId(id);
                return statusDao.execute();
            } catch (WeiboException e) {
                Logger.logExcpetion(e);
                Utilities.notice(e.getMessage());
                cancel(true);
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(WeiboStatus result) {
            startActivity(ActivityUtils.weiboActivity(result));
            finish();
        }
    }
}
