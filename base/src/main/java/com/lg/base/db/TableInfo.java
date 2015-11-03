package com.lg.base.db;

import java.util.Map;

/**
 * Created by liguo on 2015/10/16.
 */
public class TableInfo {
    private String className;
    private String tableName;
    private String primaryKeyColumnName;
    private String primaryKeyFieldName;
    private Map<String,FieldInfo> fieldInfoMap;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, FieldInfo> getFieldInfoMap() {
        return fieldInfoMap;
    }

    public void setFieldInfoMap(Map<String, FieldInfo> fieldInfoMap) {
        this.fieldInfoMap = fieldInfoMap;
    }

    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    public void setPrimaryKeyColumnName(String primaryKeyColumnName) {
        this.primaryKeyColumnName = primaryKeyColumnName;
    }

    public String getPrimaryKeyFieldName() {
        return primaryKeyFieldName;
    }

    public void setPrimaryKeyFieldName(String primaryKeyFieldName) {
        this.primaryKeyFieldName = primaryKeyFieldName;
    }
}
