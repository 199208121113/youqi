package com.lg.base.dialog;

import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by 007 on 2015/3/25.
 */
public class ProxyOnClickListener implements DialogInterface.OnClickListener {
    final DialogCallBack mCallBack;
    /** 0:取消   1:确定 */
    final int btnId;
    final Bundle params;
    public ProxyOnClickListener(DialogCallBack mCallBack,int btnId,Bundle params) {
        this.mCallBack = mCallBack;
        this.btnId = btnId;
        this.params = params;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(mCallBack == null)
            return;
        if(btnId == 1){
            mCallBack.onOK(params);
        }else if(btnId == 0){
            mCallBack.onCancel(params);
        }
    }
    public static interface DialogCallBack{
        void onOK(Bundle params);
        void onCancel(Bundle params);
    }
}
