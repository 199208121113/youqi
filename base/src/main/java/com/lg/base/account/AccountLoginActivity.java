package com.lg.base.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import com.lg.base.core.BaseApplication;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class AccountLoginActivity extends AccountAuthenticatorActivity {

	public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
	public static final String PARAM_USERNAME = "user_name";
	public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
	protected static final String TAG = AccountLoginActivity.class.getSimpleName();
	private boolean confirmCredentials = false;
	private String username = null;
	private String password = null;
	private String authTokenType = null;
	private AccountManager accountManager = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accountManager = AccountManager.get(this);
		final Intent it = getIntent();
		username = it.getStringExtra(PARAM_USERNAME);
		authTokenType = it.getStringExtra(PARAM_AUTHTOKEN_TYPE);
		confirmCredentials = it.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

	}

	protected final void onLoginSuccess(String username,String password) {
		if (!confirmCredentials)
			finishLogin(username, password);
		else
			finishConfirmCredentials(true);
	}

	private void finishLogin(final String username, final String password) {
		String accountType = BaseApplication.getAppInstance().getAccountType();
		final Intent intent = new Intent();
		intent.putExtra(KEY_ACCOUNT_NAME, username);
		intent.putExtra(KEY_ACCOUNT_TYPE, accountType);
		if (accountType.equals(authTokenType)) {
			intent.putExtra(KEY_AUTHTOKEN, password);
		}
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
	}

	private void finishConfirmCredentials(boolean result) {
		String accountType = BaseApplication.getAppInstance().getAccountType();
		final Account account = new Account(username, accountType);
		accountManager.setPassword(account, password);
		final Intent intent = new Intent();
		intent.putExtra(KEY_BOOLEAN_RESULT, result);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
	}
}