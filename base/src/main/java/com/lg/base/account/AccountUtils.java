package com.lg.base.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;

import com.lg.base.core.BaseApplication;
import com.lg.base.bus.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountUtils {
	private static final String TAG = AccountUtils.class.getSimpleName();

	public static Account getAccount(final AccountManager manager, final Activity activity) throws IOException, AccountsException {
		LogUtil.d(TAG, "Getting account");
		if (activity == null)
			throw new IllegalArgumentException("Activity cannot be null");
		if (activity.isFinishing())
			throw new OperationCanceledException();
		Account[] accounts = null;
		try {
			if (!hasAuthenticator(manager))
				throw new AuthenticatorConflictException();
			while ((accounts = getAccounts(manager)).length == 0) {
				LogUtil.d(TAG, "No njl accounts for activity=" + activity);
				//这一步会跳转到登录页面
				Bundle result = manager.addAccount(getACCOUNT_TYPE(), null, null, null, activity, null, null).getResult();
				LogUtil.d(TAG, "Added account " + result.getString(AccountManager.KEY_ACCOUNT_NAME));
			}
		} catch (Exception e) {
			throw e;
		}
		return accounts[0];
	}

	public static boolean hasAuthenticator(final AccountManager manager) {
		if (AUTHENTICATOR_CHECKED)
			return HAS_AUTHENTICATOR;
		final AuthenticatorDescription[] types = manager.getAuthenticatorTypes();
		if (types != null && types.length > 0) {
			for (AuthenticatorDescription descriptor : types) {
				if (descriptor != null && getACCOUNT_TYPE().equals(descriptor.type)) {
					HAS_AUTHENTICATOR = getACCOUNT_TYPE().equals(descriptor.packageName);
					break;
				}
			}
		}
		AUTHENTICATOR_CHECKED = true;
		return HAS_AUTHENTICATOR;
	}

	public static Account[] getAccounts(final AccountManager manager) throws OperationCanceledException, AuthenticatorException, IOException {
		final AccountManagerFuture<Account[]> future = manager.getAccountsByTypeAndFeatures(getACCOUNT_TYPE(), null, null, null);
		final Account[] accounts = future.getResult();
		if (accounts != null && accounts.length > 0)
			return getPasswordAccessibleAccounts(manager, accounts);
		else
			return new Account[0];
	}

    public static Account getAccount(final AccountManager manager) throws OperationCanceledException, AuthenticatorException, IOException {
        Account accounts[] = manager.getAccountsByType(getACCOUNT_TYPE());
        if(accounts == null || accounts.length == 0){
            return null;
        }
        return accounts[0];
    }

	private static Account[] getPasswordAccessibleAccounts(final AccountManager manager, final Account[] candidates) throws AuthenticatorConflictException {
		final List<Account> accessible = new ArrayList<Account>(candidates.length);
		boolean exceptionThrown = false;
		for (Account account : candidates)
			try {
				manager.getPassword(account);
				accessible.add(account);
			} catch (SecurityException ignored) {
				exceptionThrown = true;
			}
		if (accessible.isEmpty() && exceptionThrown)
			throw new AuthenticatorConflictException();
		return accessible.toArray(new Account[accessible.size()]);
	}

    private static String getACCOUNT_TYPE() {
        return BaseApplication.getAppInstance().getAccountType();
    }
	private static boolean AUTHENTICATOR_CHECKED = false;
	private static boolean HAS_AUTHENTICATOR = false;

	private static class AuthenticatorConflictException extends IOException {
		private static final long serialVersionUID = 1L;
	}
}
