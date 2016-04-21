package com.lg.test.account;

import android.accounts.AccountManager;

import com.lg.base.core.BaseAsyncTask;

/**
 * Created by liguo on 2015/10/15.
 */
public class UserLoginTask extends BaseAsyncTask<Boolean> {
    private String username;
    private String pwd;

    public UserLoginTask(String uid,String pwd) {
        super();
        this.username = uid;
        this.pwd = pwd;
    }

    @Override
    protected Boolean run() throws Exception {
        AccountManager am = AccountManager.get(getCtx());
        boolean added = createOrUpdateAccount(this.username,this.pwd,am);
        return added;
    }

    public String getUsername() {
        return username;
    }

    public String getPwd() {
        return pwd;
    }
}
