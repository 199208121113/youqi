package com.lg.base.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.google.inject.Inject;
import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.utils.StringUtil;

import java.util.concurrent.Executor;

public abstract class AccountAuthenticatedTask<ResultT> extends BaseRoboAsyncTask<ResultT> {

	@SuppressWarnings("unused")
	private final String TAG = AccountAuthenticatedTask.class.getSimpleName();

	public AccountAuthenticatedTask(Context context, Executor executor) {
		super(context, executor);
	}

	public AccountAuthenticatedTask(Context context, Handler handler, Executor executor) {
		super(context, handler, executor);
	}

	public AccountAuthenticatedTask(Context context, Handler handler) {
		super(context, handler);
	}

	public AccountAuthenticatedTask(Context context) {
		super(context);
	}

	@Inject
	protected Activity activity;


	private String loginPwd = null;
	private String loginedUserId = null;

	@Override
	protected ResultT run() throws Exception {
		final AccountManager accountManager = AccountManager.get(activity);
		Account account = AccountUtils.getAccount(accountManager, activity);

		loginedUserId = account.name;
		loginPwd = getPasswordFromAccount(account,accountManager);
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

	protected abstract ResultT run(Account account) throws Exception;

	public String getLoginPwd() {
		return loginPwd;
	}

	public String getLoginedUserId() {
		return loginedUserId;
	}
}
