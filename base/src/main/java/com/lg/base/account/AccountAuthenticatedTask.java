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

	private String pwd = null;
	private String uid = null;

	@Override
	protected final T run() throws Exception {
		final AccountManager accountManager = AccountManager.get(getActivityContext());
		Account account = AccountUtils.getAccount(accountManager, getActivityContext());

		uid = account.name;
		pwd = getPasswordFromAccount(account,accountManager);
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
		}
		return pwd;
	}

	@Override
	protected int getRetryCount() {
		return 0;
	}

	protected abstract T run(Account account) throws Exception;

}
