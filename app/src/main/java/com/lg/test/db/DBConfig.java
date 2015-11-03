package com.lg.test.db;

import android.content.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Created by liguo on 2015/10/17.
 */
public class DBConfig extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    DBHelper dbHelper(Context context){
        return new DBHelper(context);
    }
}
