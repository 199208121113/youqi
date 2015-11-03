package com.lg.base.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lg.base.core.LogUtil;
import com.lg.base.utils.StringUtil;

/**
 * Created by liguo on 2015/10/16.
 */
public abstract class DatabaseHelper extends SQLiteOpenHelper {
    protected final String TAG = this.getClass().getSimpleName();
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    protected void createTable(Class<?> cls,SQLiteDatabase db) throws Exception{
        TableInfo tableInfo = DBUtil.getTableInfo(cls);
        String sql = DBUtil.buildCreateTableSQL(tableInfo);
        LogUtil.e(TAG, "createTable(),sql=" + sql);
        db.execSQL(sql);
    }

    protected void dropTable(Class<?> cls) throws Exception{
        TableInfo tableInfo = DBUtil.getTableInfo(cls);
        if(tableInfo == null){
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("drop table ?", new Object[]{tableInfo.getTableName()});
    }

    public <ID,DATA> Dao<ID,DATA> getDao(Class<DATA> cls) throws Exception{
        TableInfo tableInfo = DBUtil.getTableInfo(cls);
        if(tableInfo == null){
            throw new Exception(cls.getName()+"is not add 'DatabaseTable' Annotation");
        }
        if(!isHasPrimaryKey(tableInfo)){
           throw new Exception("must set a primary key for table "+tableInfo.getTableName());
        }
        return new Dao<ID,DATA>(this.getWritableDatabase(),tableInfo);
    }

    /** 检查表中是否包含主键 */
    private boolean isHasPrimaryKey(TableInfo tableInfo){
        return StringUtil.isNotEmpty(tableInfo.getPrimaryKeyColumnName());
    }
}
