package com.lg.test.db;

import android.content.Context;

import com.google.inject.Inject;
import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.core.LogUtil;
import com.lg.test.module.User;

import java.util.List;

/**
 * Created by liguo on 2015/10/15.
 */
public class UserOpTask extends BaseRoboAsyncTask<Boolean> {
    private String username;
    private String pwd;

    @Inject
    UserDao userDao;
    public UserOpTask(Context context, String username, String pwd) {
        super(context);
        this.username = username;
        this.pwd = pwd;
    }

    @Override
    protected Boolean run() throws Exception {
        User user = new User();
        user.setUid(0);
        user.setName(username);
        user.setPwd(pwd);

        boolean createdOrUpdated = userDao.saveOrUpdate(user);
        LogUtil.e(tag,"createdOrUpdated="+createdOrUpdated);

        List<User> userList = userDao.queryAll();
        if(userList != null && userList.size()>0){
            for (User u : userList){
                LogUtil.e(tag,"uid="+u.getUid()+",name="+u.getName()+",pwd="+u.getPwd());
            }
        }
        User oldUser = userDao.getDao().queryByID(user.getUid());
        LogUtil.e(tag,"oldUser.isNull()="+(oldUser==null));

        boolean deleted = userDao.getDao().deleteById(user.getUid());
        LogUtil.e(tag,"deleted="+deleted);

        long totalCount = userDao.getDao().countOf();
        LogUtil.e(tag, "totalCount=" + totalCount);


        return true;
    }
}
