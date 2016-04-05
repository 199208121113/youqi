package com.lg.test.sms;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.lg.base.bus.BaseEvent;
import com.lg.base.core.BaseService;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventLocation;
import com.lg.base.bus.LogUtil;
import com.lg.base.utils.ToastUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短信数据库监听service
 * Created by liguo on 2015/11/5.
 */
public class SmsService extends BaseService {
    SmsContentObserver mObserver = null;
    public static final AtomicInteger ai = new AtomicInteger(100);
    public static final int SMS_START_SUCCESS = ai.getAndIncrement();

    @Override
    public void onCreate() {
        super.onCreate();
        mObserver = new SmsContentObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);
        EventBus.get().sendEvent(new BaseEvent(EventLocation.any, SMS_START_SUCCESS));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    //------------------------------------------------------------------------------------------------
    /**
     * _id：短信序号，如100

     thread_id：对话的序号，如100，与同一个手机号互发的短信，其序号是相同的

     address：发件人地址，即手机号，如+86138138000

     person：发件人，如果发件人在通讯录中则为具体姓名，陌生人为null

     date：日期，long型，如1346988516，可以对日期显示格式进行设置

     protocol：协议0SMS_RPOTO短信，1MMS_PROTO彩信

     read：是否阅读0未读，1已读

     status：短信状态-1接收，0complete,64pending,128failed

     type：短信类型1是接收到的，2是已发出

     body：短信具体内容

     service_center：短信服务中心号码编号，如+8613800755500
     */
    private class SmsContentObserver extends ContentObserver{
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ContentResolver resolver = getContentResolver();
            String selection = "type=?";
            String[] selectionArgs = {"1"};
            Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "body", "date"}, selection, selectionArgs, "_id desc");
            long id = -1;
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(0);
                String address = cursor.getString(1);
                String body = cursor.getString(2);
                long date = cursor.getLong(3);
                LogUtil.e(tag, "address=" + address + ",body=" + body + ",date=" + (new Date(date)).toLocaleString());
            }
            if (cursor != null) {
                cursor.close();
            }

            if (id != -1) {
                int count = resolver.delete(Uri.parse("content://sms"), "_id=" + id, null);
                ToastUtil.show(SmsService.this, count == 1 ? "删除成功" : "删除失败");
            }
        }
    }
}
