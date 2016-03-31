package com.lg.base.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;

import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.utils.StringUtil;

public abstract class AccountAuthenticatedTask<T> extends BaseRoboAsyncTask<T> {

	public AccountAuthenticatedTask(Activity activity) {
		super(activity);
	}

	@Override
	protected final T run() throws Exception {
		final AccountManager accountManager = AccountManager.get(getWeakActivity());
		Account account = AccountUtils.getAccount(accountManager, getWeakActivity());
		return run(account);
	}

	protected String getPasswordFromAccount(Account account,AccountManager accountManager){
		String pwd = "";
		try {
			pwd = accountManager.getPassword(account);
			if(StringUtil.isEmpty(pwd)){
                String newPwd = accountManager.getUserData(account,"pwd");
                if(newPwd != null && newPwd.trim().length() > 0){
					pwd = newPwd;
                }
            }
		} catch (Exception e) {
			//ignored
			e.printStackTrace();
		}
		return pwd;
	}

	@Override
	protected int getRetryCount() {
		return 0;
	}

	protected abstract T run(Account account) throws Exception;

}
