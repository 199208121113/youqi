package com.lg.base.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.lg.base.bus.LogUtil;
import com.lg.base.core.BaseApplication;
import com.lg.base.utils.StringUtil;

public class AccountAuthenticator extends AbstractAccountAuthenticator{

	private static final String TAG = AccountAuthenticator.class.getSimpleName();

	private Context context = null;
	private final AccountConfig ac;
	public AccountAuthenticator(Context context,AccountConfig ac) {
		super(context);
		this.context = context;
		this.ac = ac;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		LogUtil.d(TAG, "addAccount()");
		final Intent intent = new Intent(context,ac.getLoginClass());
		intent.putExtra(AccountLoginActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		LogUtil.d(TAG, "getAuthToken()");
		final Bundle bundle = new Bundle();
		if (getAccountType().equals(authTokenType))
			return bundle;
		AccountManager am = AccountManager.get(context);
		String password = am.getPassword(account);
		if (TextUtils.isEmpty(password)) {
			bundle.putParcelable(AccountManager.KEY_INTENT, createLoginIntent(response));
			return bundle;
		}

		String authToken = null;
		try {
			authToken = getAuthorizationFromServer();
		} catch (Exception e) {
			LogUtil.e(TAG, "Authorization retrieval failed", e);
			throw new NetworkErrorException(e);
		}

		if (StringUtil.isEmpty(authToken))
			bundle.putParcelable(AccountManager.KEY_INTENT, createLoginIntent(response));
		else {
			bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, getAccountType());
			bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			am.clearPassword(account);
		}
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		String accountType = getAccountType();
		return accountType.equals(authTokenType) ? accountType : null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		final Intent intent = new Intent(context,ac.getLoginClass());
		intent.putExtra(AccountLoginActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		if (!TextUtils.isEmpty(account.name))
			intent.putExtra(AccountLoginActivity.PARAM_USERNAME, account.name);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		final Bundle result = new Bundle();
		result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
		return result;
	}

	// ================================================================
	private Intent createLoginIntent(final AccountAuthenticatorResponse response) {
		final Intent intent = new Intent(context, AccountLoginActivity.class);
		intent.putExtra(AccountLoginActivity.PARAM_AUTHTOKEN_TYPE, getAccountType());
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		return intent;
	}

	private String getAuthorizationFromServer() throws Exception {
		return "HelloServer";
	}

	private String getAccountType(){
		return BaseApplication.getAppInstance().getAccountType();
	}
}
