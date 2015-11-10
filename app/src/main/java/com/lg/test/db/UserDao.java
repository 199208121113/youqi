package com.lg.test.db;

import com.lg.base.db.Dao;
import com.lg.test.module.User;

import java.util.List;

/**
 * Created by liguo on 2015/10/17.
 */


public class UserDao {

    private Dao<Long,User> dao;

    public boolean saveOrUpdate(User user) throws Exception{
        return getDao().createOrUpdate(user);
    }

    public List<User> queryAll() throws Exception{
        return getDao().queryAll();
    }

    public Dao<Long,User> getDao() throws Exception{
        if(dao == null){
            dao = DBHelper.getInstance().getDao(User.class);
        }
        return dao;
    }

    private static UserDao userDao = null;
    public static UserDao getInstance(){
        if(userDao == null){
            userDao = new UserDao();
        }
        return userDao;
    }
}
