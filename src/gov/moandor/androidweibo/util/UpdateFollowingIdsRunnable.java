package gov.moandor.androidweibo.util;

import java.util.ArrayList;
import java.util.List;

import gov.moandor.androidweibo.bean.Account;
import gov.moandor.androidweibo.dao.FriendsIdsDao;

public class UpdateFollowingIdsRunnable implements Runnable {
    @Override
    public void run() {
        for (Account account : GlobalContext.getAccounts()) {
            updateFollowingIds(account);
        }
    }
    
    private static void updateFollowingIds(Account account) {
        if (DatabaseUtils.getFollowingIds(account.user.id) != null && !GlobalContext.isInWifi()) {
            return;
        }
        FriendsIdsDao dao = new FriendsIdsDao();
        dao.setToken(account.token);
        dao.setUserId(account.user.id);
        dao.setCount(5000);
        try {
            List<Long> ids = new ArrayList<Long>();
            int nextCursor = 0;
            do {
                dao.setCursor(nextCursor);
                ids.addAll(dao.fetchData());
                nextCursor = dao.getNextCursor();
            } while (nextCursor != 0);
            long[] result = new long[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                result[i] = ids.get(i);
            }
            DatabaseUtils.updateFollowingIds(result, account.user.id);
        } catch (WeiboException e) {
            Logger.logExcpetion(e);
        }
    }
}
