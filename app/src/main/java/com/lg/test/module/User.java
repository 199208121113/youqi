package com.lg.test.module;

import com.lg.base.db.DatabaseField;
import com.lg.base.db.DatabaseTable;

/**
 * Created by liguo on 2015/10/17.
 */
@DatabaseTable(tableName = "user_info")
public class User {

    @DatabaseField(columnName = "u_id",id = true)
    private long uid;

    @DatabaseField(columnName = "u_name")
    private String name;

    @DatabaseField()
    private String pwd;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
