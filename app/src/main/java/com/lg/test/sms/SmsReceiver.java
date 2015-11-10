package com.lg.test.sms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.lg.base.core.BaseReceiver;
import com.lg.base.core.LogUtil;

/**
 * 需要把我们自己的App设置为默认的短信应用程序才能[阻止广播继续下发|删除短信记录]
 * 短信Receiver
 * Created by liguo on 2015/11/5.
 */
public class SmsReceiver extends BaseReceiver {
    @Override
    protected void doReceive(Context context, Intent intent) {
        StringBuilder body = new StringBuilder();// 短信内容
        StringBuilder number = new StringBuilder();// 短信发件人
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] data = (Object[]) bundle.get("pdus");
            if(data == null || data.length == 0)
                return;
            SmsMessage[] message = new SmsMessage[data.length];
            for (int i = 0; i < data.length; i++) {
                message[i] = SmsMessage.createFromPdu((byte[]) data[i]);
            }
            for (SmsMessage currentMessage : message) {
                body.append(currentMessage.getDisplayMessageBody());
                number.append(currentMessage.getDisplayOriginatingAddress());
            }
            String smsBody = body.toString();
            String smsNumber = number.toString();
            LogUtil.e(tag,"smsBody="+smsBody+",smsNumber="+smsNumber);
            this.abortBroadcast();
        }
    }
}
