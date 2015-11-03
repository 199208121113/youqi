package com.lg.test.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;
import com.lg.base.core.LogUtil;
import com.lg.base.db.DatabaseHelper;
import com.lg.test.module.User;

/**
 * Created by liguo on 2015/10/17.
 */
public class DBHelper extends DatabaseHelper {
    private static final String DB_NAME = "lg_test.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Inject
    public DBHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
        LogUtil.e(TAG,"DBHelper init success");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.e(TAG,"onCreate()");
        try {
            createTable(User.class,db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
