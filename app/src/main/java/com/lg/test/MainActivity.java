package com.lg.test;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.lg.base.core.BaseActivity;
import com.lg.base.core.BaseEvent;
import com.lg.base.core.LogUtil;
import com.lg.base.core.UITask;
import com.lg.base.task.download.SimpleFileDownloadTask;
import com.lg.base.task.upload.SimpleFileUploadTask;
import com.lg.base.utils.NumberUtil;
import com.lg.base.utils.StringUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.account.CollectTask;
import com.lg.test.activity.TestQrCodeActivity;
import com.lg.test.db.UserOpActivity;
import com.lg.test.sms.SmsService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import roboguice.inject.InjectView;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    /** 帐户测试 */
    @InjectView(R.id.act_main_test_account_tv)
    View testAccountView;

    /** 数据库测试 */
    @InjectView(R.id.act_main_test_db_tv)
    View testDBView;

    /** 文件上传测试 */
    @InjectView(R.id.act_main_test_file_upload)
    TextView testUploadFile;

    /** 文件下载测试 */
    @InjectView(R.id.act_main_test_file_download)
    TextView testDownloadFile;

    /** 二维码测试 */
    @InjectView(R.id.act_main_test_qr_code)
    TextView testQrCode;

    @InjectView(R.id.act_main_test_sms)
    TextView testSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testAccountView.setOnClickListener(this);
        testDBView.setOnClickListener(this);
        testUploadFile.setOnClickListener(this);
        testDownloadFile.setOnClickListener(this);
        testQrCode.setOnClickListener(this);
        testSms.setOnClickListener(this);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    public void executeEvent(BaseEvent evt) {
        super.executeEvent(evt);
        if(evt.getWhat() == SmsService.SMS_START_SUCCESS){
            postRunOnUi(new UITask(this) {
                @Override
                public void run() {
                    ToastUtil.show(getContext(),"SMS Service started!");
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(v == testAccountView){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings");
            intent.setComponent(cn);
            intent.putExtra(":android:show_fragment", "com.android.settings.applications.AppOpsSummary");
            startActivity(intent);
            collect();
        }else if(v == testDBView){
            startActivity(UserOpActivity.createIntent(this));
        }else if(v == testUploadFile){
            upload();
        }else if(v == testDownloadFile){
            download();
        }else if(v == testQrCode){
            startActivity(TestQrCodeActivity.createIntent(this));
        }else if(v == testSms){
            // 需要把我们自己的App设置为默认的短信应用程序才能[阻止广播继续下发|删除短信记录]
//            startService(new Intent(this, SmsService.class));
        }
    }

    /** 收藏 */
    private void collect(){
        new CollectTask(this){
            @Override
            protected void onSuccess(String s) throws Exception {
                super.onSuccess(s);
                ToastUtil.show(getContext(), "account.name=" + s);
            }
        }.execute();
    }

    private void upload(){
        String uploadLoadUrl = "https://www.ireadercity.com/GoodBooks/iOSServices/BookUpLoad.aspx";
        String pp = Environment.getExternalStorageDirectory().getAbsolutePath()+"/1001.jpg";
        File uploadFile = new File(pp);
        Map<String,String> params = getGenericParams();
        params.put("bookTitle","test-bookTitle");
        params.put("bookDesc", "test-bookDesc");
        params.put("bookAuthor", "test-bookAuthor");
        params.put("bookUploadedUserID", "liguo6568");
        new SimpleFileUploadTask(this,uploadLoadUrl,uploadFile,params){
            @Override
            protected void onSuccess(String s) throws Exception {
                super.onSuccess(s);
                ToastUtil.show(getContext(), "上传成功");
            }

            @Override
            protected void onProgressChanged(long handBytes, long totalBytes) {
                LogUtil.e(TAG, "uploadedBytes=" + handBytes + ",totalBytes=" + totalBytes);
                Bundle bd = new Bundle();
                bd.putLong("rec", handBytes);
                bd.putLong("total", totalBytes);
                if(totalBytes > 0) {
                    bd.putString("scale", NumberUtil.format2(handBytes * 100f / totalBytes));
                }
                postRunOnUi(new UITask(getContext(), bd) {
                    @Override
                    public void run() {
                        Bundle bd = getExtra();
                        String scale = bd.getString("scale");
                        if(StringUtil.isNotEmpty(scale)) {
                            testUploadFile.setText(scale + "%");
                        }else{
                            long handBytes = getExtra().getLong("rec");
                            testUploadFile.setText(NumberUtil.format2((handBytes * 1f) / (1024 * 1024))+ "MB");
                        }
                    }
                });
            }
        }.execute();
    }

    private void download(){
        String url = "http://andfls.qiniudn.com/AIReader.apk";
        String pp = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AIReader.apk";
        new SimpleFileDownloadTask(this,url,pp){
            @Override
            protected void onSuccess(String t) throws Exception {
                super.onSuccess(t);
                ToastUtil.show(getContext(), "下载成功");
            }

            @Override
            public void onProgressChanged(long handBytes, long totalBytes) {
                LogUtil.e(TAG, "handBytes=" + handBytes + ",totalBytes=" + totalBytes);
                Bundle bd = new Bundle();
                bd.putLong("rec", handBytes);
                bd.putLong("total", totalBytes);
                if(totalBytes > 0) {
                    bd.putString("scale", NumberUtil.format2(handBytes * 100f / totalBytes));
                }
                postRunOnUi(new UITask(this.getContext(), bd) {
                    @Override
                    public void run() {
                        String scale = getExtra().getString("scale");
                        if (StringUtil.isNotEmpty(scale)) {
                            testDownloadFile.setText(scale + "%");
                        }else{
                            long handBytes = getExtra().getLong("rec");
                            testDownloadFile.setText(NumberUtil.format2((handBytes *1f) / (1024*1024)) + "MB");
                        }
                    }
                });
            }
        }.execute();
    }

    public static Map<String,String> getGenericParams(){
        Map<String,String> pm = new HashMap<>();
        pm.put("appID", "com.youloft.glsc");
        pm.put("passID", "books by AireaderCity_1234567890");
        pm.put("deviceID", "5ecc403e012bf23b391146f0438b5863");
        pm.put("deviceType", "Android");
        pm.put("ver", "4.65");
        pm.put("appPackageName","com.ireadercity");
        pm.put("clientVersion", "5.2");
        return pm;
    }
}
