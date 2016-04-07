package com.lg.test.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lg.base.core.BaseApplication;
import com.lg.test.greendao.DaoMaster;
import com.lg.test.greendao.DaoSession;

/**
 * Created by root on 16-4-7.
 */
public class DBHelper {
    private static final String DB_NAME = "notes-db";
    private volatile SQLiteDatabase db = null;
    private volatile DaoMaster daoMaster = null;
    private volatile DaoSession daoSession = null;
    private DBHelper(Context ctx) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, DB_NAME, null);
        db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private static class InnerDBHelper{
        private static final DBHelper INSTANCE = new DBHelper(BaseApplication.getAppInstance());
    }
    public static DBHelper get(){
        return InnerDBHelper.INSTANCE;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
