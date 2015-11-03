package com.lg.base.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by 007 on 2015/3/25.
 */
public class ProxyOnDismissListener implements DialogInterface.OnDismissListener {
    private final DialogCloseCallBack closeCallBack;
    private final Bundle params;
    private Context mContext = null;
    public ProxyOnDismissListener(Context ctx,DialogCloseCallBack closeCallBack,Bundle params) {
        this.mContext = ctx;
        this.closeCallBack = closeCallBack;
        this.params = params;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(closeCallBack != null){
            closeCallBack.onDismiss(this.mContext,this.params);
        }
    }
    public static interface DialogCloseCallBack{
        void onDismiss(Context mContext,Bundle params);
    }
}
