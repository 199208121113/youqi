package com.lg.base.account;

/**
 * Created by liguo on 2015/10/14.
 */
public interface AccountConfig {
    Class<?> getLoginClass();

    //<?xml version="1.0" encoding="utf-8"?>
    /*
     * 集成步骤
     * (1)new MyAccountService extends AccountAuthenticatorService 并实现buildAccountConfig()方法
     * (2)在res目录下新建xml/authenticator.xml内容如下：
         <account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
         android:accountType="@string/cur_package_name"
         android:icon="@drawable/app_icon_shuxiang"
         android:label="@string/app_name"
         android:smallIcon="@drawable/app_icon_shuxiang" />
       (3)在AndroidManifest.xml中配置
         <service
         android:name=".xxx.MyAccountService"
         android:exported="false"
         android:process=":auth" >
         <intent-filter>
         <action android:name="android.accounts.AccountAuthenticator" />
         </intent-filter>

         <meta-data
         android:name="android.accounts.AccountAuthenticator"
         android:resource="@xml/authenticator" />
         </service>
       (4)新建一个Activity并继承自BaseLoginActivity
     */
}
