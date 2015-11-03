package com.lg.test.account;

import com.lg.base.account.AccountAuthenticatorService;
import com.lg.base.account.AccountConfig;

/**
 * Created by liguo on 2015/10/14.
 */
public class MyAccountService extends AccountAuthenticatorService {
    @Override
    protected AccountConfig buildAccountConfig() {
        return getAccountConfig();
    }

    private static AccountConfig getAccountConfig(){
        return new AccountConfig() {
            @Override
            public Class<?> getLoginClass() {
                return LoginActivity.class;
            }
        };
    }
}
