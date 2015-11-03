package com.lg.base.account;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class AccountAuthenticatorService extends Service {

	private static AccountAuthenticator AUTHENTICATOR = null;

	@Override
	public IBinder onBind(Intent intent) {
		return intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT) ? getAuthenticator().getIBinder() : null;
	}

	private AccountAuthenticator getAuthenticator() {
		if (AUTHENTICATOR == null)
			AUTHENTICATOR = new AccountAuthenticator(this,buildAccountConfig());
		return AUTHENTICATOR;
	}

	protected abstract AccountConfig buildAccountConfig();
}
