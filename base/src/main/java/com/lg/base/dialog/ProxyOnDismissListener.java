package com.lg.base.dialog;

import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by 007 on 2015/3/25.
 */
public class ProxyOnDismissListener implements DialogInterface.OnDismissListener {
    private final DialogCloseCallBack closeCallBack;
    private final Bundle params;
    public ProxyOnDismissListener(DialogCloseCallBack closeCallBack,Bundle params) {
        this.closeCallBack = closeCallBack;
        this.params = params;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(closeCallBack != null){
            closeCallBack.onDismiss(this.params);
        }
    }
    public interface DialogCloseCallBack{
        void onDismiss(Bundle params);
    }
}
