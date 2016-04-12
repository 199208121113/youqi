package com.lg.test.core;

import com.lg.base.core.BaseApplication;
import com.zhy.changeskin.SkinManager;

/**
 * Created by liguo on 2015/10/15.
 */
public class MyBaseApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.getInstance().init(this);
    }

    /*@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}*/
}
