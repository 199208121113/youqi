package com.lg.test.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lg.base.db.Dao;
import com.lg.test.module.User;

import java.util.List;

/**
 * Created by liguo on 2015/10/17.
 */

@Singleton
public class UserDao {

    @Inject
    private DBHelper dbHelper;

    private Dao<Long,User> dao;

    public boolean saveOrUpdate(User user) throws Exception{
        return getDao().createOrUpdate(user);
    }

    public List<User> queryAll() throws Exception{
        return getDao().queryAll();
    }

    public Dao<Long,User> getDao() throws Exception{
        if(dao == null){
            dao = dbHelper.getDao(User.class);
        }
        return dao;
    }
}
