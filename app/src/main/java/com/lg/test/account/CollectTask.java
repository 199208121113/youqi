package com.lg.test.account;

import android.accounts.Account;
import android.content.Context;

import com.lg.base.account.AccountAuthenticatedTask;

/**
 * Created by liguo on 2015/10/15.
 */
public class CollectTask extends AccountAuthenticatedTask<String> {
    public CollectTask(Context context) {
        super(context);
    }

    @Override
    protected String run(Account account) throws Exception {
        return account.name;
    }
}
