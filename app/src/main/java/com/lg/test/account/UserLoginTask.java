package com.lg.test.account;

import android.accounts.AccountManager;
import android.content.Context;

import com.lg.base.core.BaseRoboAsyncTask;

/**
 * Created by liguo on 2015/10/15.
 */
public class UserLoginTask extends BaseRoboAsyncTask<Boolean> {
    private String username;
    private String pwd;

    public UserLoginTask(Context context,String uid,String pwd) {
        super(context);
        this.username = uid;
        this.pwd = pwd;
    }

    @Override
    protected Boolean run() throws Exception {
        AccountManager am = AccountManager.get(getContext());
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
