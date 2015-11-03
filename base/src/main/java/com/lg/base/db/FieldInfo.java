package com.lg.base.db;

/**
 * Created by liguo on 2015/10/16.
 */
public class FieldInfo {
    String fieldName;
    Class<?> fieldType;
    String columnName;
    DataType columnType;
    boolean id;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DataType getColumnType() {
        return columnType;
    }

    public void setColumnType(DataType columnType) {
        this.columnType = columnType;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }
}
