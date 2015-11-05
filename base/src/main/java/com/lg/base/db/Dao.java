package com.lg.base.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lg.base.utils.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liguo on 2015/10/16.
 */
public class Dao<ID,DATA> {
    private static final String TAG = "Dao";
    final SQLiteDatabase db;
    final TableInfo tableInfo;
    private Class<?> dataClass = null;

    public Dao(SQLiteDatabase db,TableInfo tableInfo) {
        this.db = db;
        this.tableInfo = tableInfo;
        try {
            this.dataClass = Class.forName(tableInfo.getClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 新增或修改 */
    public boolean createOrUpdate(DATA data) throws Exception{
        Field f = getField(tableInfo.getPrimaryKeyFieldName());
        ID id = (ID)f.get(data);
        if(!isValid(id)){
            return false;
        }
        boolean isExists = isExistsById(id);
        boolean result;
        if(isExists){
            result = update(data);
        }else{
            result = create(data);
        }
        return result;
    }

    /** 新增 */
    public boolean create(DATA data) throws Exception{
        Map<String,FieldInfo> fieldInfoMap = tableInfo.getFieldInfoMap();
        List<FieldInfo> fieldInfoList = new ArrayList<>(fieldInfoMap.values());
        ContentValues cv = new ContentValues();
        for (FieldInfo fi : fieldInfoList){
            Field ff = getField(fi.getFieldName());
            Object objValue = ff.get(data);
            if(tableInfo.getPrimaryKeyColumnName().equals(fi.getColumnName())){
                if(fi.getColumnType() == DataType.INTEGER){
                    if(objValue == null) {
                        continue;
                    }else{
                        long dataId = (long)objValue;
                        if(dataId <= 0){
                            continue;
                        }
                    }
                }else if(fi.getColumnType() == DataType.TEXT){
                    if(objValue == null || StringUtil.isEmpty(objValue.toString())) {
                        objValue = UUID.randomUUID().toString();
                    }
                }
            }
            if(objValue instanceof String){
                cv.put(fi.getColumnName(),(String)objValue);
            }else if(objValue instanceof Byte){
                cv.put(fi.getColumnName(),(byte)objValue);
            }else if(objValue instanceof Short){
                cv.put(fi.getColumnName(),(short)objValue);
            }else if(objValue instanceof Integer){
                cv.put(fi.getColumnName(),(int)objValue);
            }else if(objValue instanceof Long){
                cv.put(fi.getColumnName(),(long)objValue);
            }else if(objValue instanceof Float){
                cv.put(fi.getColumnName(),(float)objValue);
            }else if(objValue instanceof Double){
                cv.put(fi.getColumnName(),(double)objValue);
            }else if(objValue instanceof Boolean){
                cv.put(fi.getColumnName(),(boolean)objValue);
            }else if(objValue instanceof byte[]){
                cv.put(fi.getColumnName(),(byte[])objValue);
            }else{
                throw new Exception("unknown type '"+objValue.getClass());
            }
        }
        return db.insert(tableInfo.getTableName(), null, cv) > 0;
    }

    /** 删除 */
    public boolean deleteById(ID id) throws Exception{
        if(!isValid(id)){
            return false;
        }
        String whereClause = tableInfo.getPrimaryKeyColumnName() + " = ?";
        String[] whereArgs = {id.toString()};
        return db.delete(tableInfo.getTableName(), whereClause, whereArgs) > 0;
    }

    /** 修改 */
    public boolean update(DATA data) throws Exception{
        Map<String,FieldInfo> fieldInfoMap = tableInfo.getFieldInfoMap();
        List<FieldInfo> fieldInfoList = new ArrayList<>(fieldInfoMap.values());
        ContentValues cv = new ContentValues();

        for (FieldInfo fi : fieldInfoList){
            if(tableInfo.getPrimaryKeyColumnName().equals(fi.getColumnName())){
               continue;
            }
            Field ff = getField(fi.getFieldName());
            Object objValue = ff.get(data);
            if(objValue == null)
                continue;
            if(objValue instanceof String){
                cv.put(fi.getColumnName(),(String)objValue);
            }else if(objValue instanceof Byte){
                cv.put(fi.getColumnName(),(byte)objValue);
            }else if(objValue instanceof Short){
                cv.put(fi.getColumnName(),(short)objValue);
            }else if(objValue instanceof Integer){
                cv.put(fi.getColumnName(),(int)objValue);
            }else if(objValue instanceof Long){
                cv.put(fi.getColumnName(),(long)objValue);
            }else if(objValue instanceof Float){
                cv.put(fi.getColumnName(),(float)objValue);
            }else if(objValue instanceof Double){
                cv.put(fi.getColumnName(),(double)objValue);
            }else if(objValue instanceof Boolean){
                cv.put(fi.getColumnName(),(boolean)objValue);
            }else if(objValue instanceof byte[]){
                cv.put(fi.getColumnName(),(byte[])objValue);
            }else{
                throw new Exception("unknown type '"+objValue.getClass());
            }
        }
        Field ff = getField(tableInfo.getPrimaryKeyFieldName());
        ID id = (ID)ff.get(data);
        String whereClause = tableInfo.getPrimaryKeyColumnName() + " = ?";
        String[] whereArgs = {id.toString()};
        return db.update(tableInfo.getTableName(), cv, whereClause, whereArgs) > 0;
    }

    /** 查询整张表的所有数据 */
    public List<DATA> queryAll() throws Exception{
        String sql = "select * from "+tableInfo.getTableName();
        return queryBySQL(sql);
    }

    /** 根据ID查询单条数据 */
    public DATA queryByID(ID id) throws Exception{
        if(!isValid(id)){
            return null;
        }
        String sql = "select * from "+tableInfo.getTableName()+" where "+tableInfo.getPrimaryKeyColumnName()+" = ? ";
        List<DATA> lst = queryBySQL(sql, id.toString());
        if(lst == null || lst.size() == 0)
            return null;
        return lst.get(0);
    }

    public List<DATA> queryBySQL(String sql,String... selectionArgs) throws Exception{
        Cursor cursor = null;
        List<DATA> dataList = null;
        try {
            cursor = getCursorBySQL(sql,selectionArgs);
            dataList = queryRaw(cursor);
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return dataList;
    }

    public List<Map<String,Object>> queryListMapBySQL(String sql,String... selectionArgs) throws Exception{
        Cursor cursor = null;
        List<Map<String,Object>> dataList = null;
        try {
            cursor = getCursorBySQL(sql,selectionArgs);
            dataList = queryMap(cursor);
        } finally {
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return dataList;
    }

    public void execBySQL(String sql,String... selectionArgs) throws Exception{
        db.execSQL(sql, selectionArgs);
    }

    /** 获取整张表共有多少条记录 */
    public long countOf() throws Exception{
        String pmKey = tableInfo.getPrimaryKeyColumnName();
        String sql = "select count('"+pmKey+"') from "+tableInfo.getTableName();
        Cursor cursor = null;
        try {
            cursor = getCursorBySQL(sql,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(cursor == null || cursor.isClosed())
            return 0;
        long num = 0;
        if(cursor.moveToFirst()){
            num = cursor.getLong(0);
        }
        cursor.close();
        return num;
    }

    /** 检查是否存在 */
    private boolean isExistsById(ID id) throws Exception{
        if(!isValid(id)){
            return false;
        }
        String pmKey = tableInfo.getPrimaryKeyColumnName();
        String sql = "select count('"+pmKey+"') from "+tableInfo.getTableName()+" where "+pmKey+" = ?";
        Cursor cursor = null;
        boolean isExists = false;
        try {
            cursor = getCursorBySQL(sql,new String[]{id.toString()});
            if(cursor == null || cursor.isClosed())
                return false;
            if(cursor.moveToFirst()){
                isExists = cursor.getLong(0) > 0;
            }
        } finally{
            if(cursor != null && !cursor.isClosed()){
                cursor.close();
            }
        }
        return isExists;
    }

    /** 根据游标查询数据 */
    private List<DATA> queryRaw(Cursor cursor) throws Exception{
        List<DATA> arrList = new ArrayList<>();
        if(cursor == null || cursor.isClosed())
            return null;
        Map<String,FieldInfo> fieldInfoMap = tableInfo.getFieldInfoMap();
        String[] columnNameArray = cursor.getColumnNames();
        while(cursor.moveToNext()){
            Object data = dataClass.newInstance();
            for (String columnName : columnNameArray){
                int columnIndex = cursor.getColumnIndex(columnName);
                FieldInfo fi = fieldInfoMap.get(columnName);
                Object objValue = DBUtil.convertCursorToObject(fi.getFieldType(), cursor, columnIndex);
                Field field = getField(fi.getFieldName());
                field.set(data,objValue);
            }
            arrList.add((DATA)data);
        }
        return arrList;
    }

    private List<Map<String,Object>> queryMap(Cursor cursor) throws Exception{
        List<Map<String,Object>> arrList = new ArrayList<>();
        if(cursor == null || cursor.isClosed())
            return null;
        Map<String,FieldInfo> fieldInfoMap = tableInfo.getFieldInfoMap();
        String[] columnNameArray = cursor.getColumnNames();
        while(cursor.moveToNext()){
            Map<String,Object> map = new LinkedHashMap<>();
            for (int i =0;i<columnNameArray.length;i++){
                String columnName =  columnNameArray[i];
                int columnIndex = cursor.getColumnIndex(columnName);
                FieldInfo fi = fieldInfoMap.get(columnName);
                Object objValue = DBUtil.convertCursorToObject(fi.getFieldType(), cursor, columnIndex);
                map.put(fi.getColumnName(),objValue);
            }
            arrList.add(map);
        }
        return arrList;
    }

    private Cursor getCursorBySQL(String sql,String[] selectionArgs){
        return db.rawQuery(sql,selectionArgs);
    }

    /** true: id有效  false:id无效 */
    private boolean isValid(ID id){
        if(id == null || StringUtil.isEmpty(id.toString())){
            return false;
        }
        return true;
    }

    private Map<String,Field> fieldMap = new HashMap<>();

    /**
     * @param fieldName 属性名，不是表的列名
     * @throws Exception
     */
    private Field getField(final String fieldName) throws Exception{
        if(fieldMap.containsKey(fieldName)){
            return fieldMap.get(fieldName);
        }
        Field field = dataClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        fieldMap.put(fieldName, field);
        return field;
    }
}
