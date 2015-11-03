package com.lg.base.db;

import android.database.Cursor;

import com.lg.base.utils.StringUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liguo on 2015/10/16.
 */
public class DBUtil {
    public static TableInfo getTableInfo(Class<?> cls) throws Exception{
        TableInfo tableInfo = new TableInfo();
        if(!cls.isAnnotationPresent(DatabaseTable.class)){
            return null;
        }
        DatabaseTable table = cls.getAnnotation(DatabaseTable.class);
        tableInfo.setClassName(cls.getName());
        tableInfo.setTableName(table.tableName());
        Map<String,FieldInfo> fieldInfoMap = new HashMap<>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields){
            DatabaseField df = field.getAnnotation(DatabaseField.class);
            if(df == null){
                continue;
            }
            FieldInfo fi = new FieldInfo();
            fi.setFieldName(field.getName());
            fi.setFieldType(field.getType());

            String fieldName = field.getName();
            boolean isId = df.id();
            String columnName = StringUtil.isNotEmpty(df.columnName()) ? df.columnName() : fieldName;
            DataType dt = df.dataType();
            if(dt == DataType.UNKNOWN){
                dt = convertDataType(field);
            }

            fi.setColumnName(columnName);
            fi.setColumnType(dt);
            fi.setId(isId);
            if(isId){
                tableInfo.setPrimaryKeyColumnName(columnName);
                tableInfo.setPrimaryKeyFieldName(fieldName);
            }
            fieldInfoMap.put(fieldName, fi);
            if(!fieldName.equals(columnName)) {
                fieldInfoMap.put(columnName, fi);
            }
        }
        tableInfo.setFieldInfoMap(fieldInfoMap);
        return tableInfo;
    }

    public static String buildCreateTableSQL(TableInfo tableInfo){
        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(tableInfo.getTableName()).append(" (");
        Map<String,FieldInfo> fieldInfoMap = tableInfo.getFieldInfoMap();
        Map<String,String> tmpMap = new HashMap<>();
        int i = 0;
        for (FieldInfo fi : fieldInfoMap.values()){
            String columnName = fi.getColumnName();
            if(tmpMap.containsKey(columnName)){
                i++;
                continue;
            }
            sb.append("["+fi.getColumnName() + "] ").append(fi.getColumnType().name() + " ");
            if(fi.isId()){
                sb.append("primary key ");
                if(fi.getColumnType() == DataType.INTEGER){
                    sb.append("autoincrement ");
                }
            }
            i++;
            if(i<fieldInfoMap.size()){
                sb.append(",");
            }
            tmpMap.put(columnName,"");
        }
        if(sb.toString().endsWith(",")){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append(")");
        return sb.toString();
    }

    public static DataType convertDataType(Field field) throws Exception{
        Class<?> type = field.getType();
        if(type == String.class || type == boolean.class || type == Boolean.class){
            return DataType.TEXT;
        }

        if(type == char.class || type == Character.class){
            return DataType.INTEGER;
        }

        if(type == byte.class || type == Byte.class){
            return DataType.INTEGER;
        }

        if(type == int.class || type == Integer.class){
            return DataType.INTEGER;
        }

        if(type == long.class || type == Long.class){
            return DataType.INTEGER;
        }

        if(type == float.class || type == Float.class){
            return DataType.FLOAT;
        }

        if(type == double.class || type == Double.class){
            return DataType.DOUBLE;
        }

        String dataTypeStr = type.getSimpleName();
        if(dataTypeStr.contains("[]")){
            if(dataTypeStr.equalsIgnoreCase("byte[]")){
                return DataType.BLOB;
            }else{
                return DataType.TEXT;
            }
        }
        throw new RuntimeException("unknown type for "+ field.getName()+",type="+dataTypeStr);
    }

    public static Object convertCursorToObject(Class<?> type,Cursor cursor,int columnIndex) throws Exception{
        if(type == String.class ){
            return cursor.getString(columnIndex);
        }

        if(type == boolean.class || type == Boolean.class){
            return Boolean.valueOf(cursor.getString(columnIndex));
        }

        if(type == char.class || type == Character.class){
            return (char)cursor.getInt(columnIndex);
        }

        if(type == byte.class || type == Byte.class){
            return (byte)cursor.getInt(columnIndex);
        }

        if(type == int.class || type == Integer.class){
            return cursor.getInt(columnIndex);
        }

        if(type == long.class || type == Long.class){
            return cursor.getLong(columnIndex);
        }

        if(type == float.class || type == Float.class){
            return cursor.getFloat(columnIndex);
        }

        if(type == double.class || type == Double.class){
            return cursor.getDouble(columnIndex);
        }

        String dataTypeStr = type.getSimpleName();
        if(dataTypeStr.contains("[]")){
            String pre = dataTypeStr.replace("[]","");
            if(pre.equalsIgnoreCase("byte")){
                return cursor.getBlob(columnIndex);
            }
        }
        throw new RuntimeException("unknown type :"+dataTypeStr);
    }
}
