package com.lg.test;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.BaseActivity;
import com.lg.base.core.InjectView;
import com.lg.base.core.LogUtil;
import com.lg.base.core.UITask;
import com.lg.base.task.download.SimpleFileDownloadTask;
import com.lg.base.task.upload.SimpleFileUploadTask;
import com.lg.base.utils.NumberUtil;
import com.lg.base.utils.StringUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.account.CollectTask;
import com.lg.test.activity.TestDbActivity;
import com.lg.test.activity.TestEncodeActivity;
import com.lg.test.activity.TestQrCodeActivity;
import com.lg.test.activity.TestRecyclerViewActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



public class MainActivity extends BaseActivity {

    /** 帐户测试 */
    @InjectView(value = R.id.act_main_test_account_tv,click = "onClick")
    View testAccountView;

    /** 数据库测试 */
    @InjectView(value = R.id.act_main_test_db_tv,click = "onClick")
    View testDBView;

    /** 文件上传测试 */
    @InjectView(value = R.id.act_main_test_file_upload,click = "onClick")
    TextView testUploadFile;

    /** 文件下载测试 */
    @InjectView(value = R.id.act_main_test_file_download,click = "onClick")
    TextView testDownloadFile;

    /** 二维码测试 */
    @InjectView(value = R.id.act_main_test_qr_code,click = "onClick")
    TextView testQrCode;

    @InjectView(value = R.id.act_main_test_encode,click = "onClick")
    TextView testEncode;

    @InjectView(value = R.id.act_main_test_recycler_view,click = "onClick")
    TextView testRecyclerView;

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("app demo");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @SuppressWarnings("all")
    public void onClick(View v) {
        if(v == testAccountView){
            collect();
        }else if(v == testDBView){
            startActivity(TestDbActivity.createIntent(this));
        }else if(v == testUploadFile){
            upload();
        }else if(v == testDownloadFile){
            download();
        }else if(v == testQrCode){
            startActivity(TestQrCodeActivity.createIntent(this));
        }else if(v == testEncode){
            startActivity(TestEncodeActivity.createIntent(this));
        }else if(v == testRecyclerView){
            startActivity(TestRecyclerViewActivity.createIntent(this));
        }
    }

    /** 收藏 */
    private void collect(){
        new CollectTask(this){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ToastUtil.show(getActivityContext(), "account.name=" + s);
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
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ToastUtil.show(getActivityContext(), "上传成功");
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
                postRunOnUi(new UITask(getActivityContext(), bd) {
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
            protected void onSuccess(String t) {
                super.onSuccess(t);
                ToastUtil.show(getActivityContext(), "下载成功");
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
                postRunOnUi(new UITask(this.getActivityContext(), bd) {
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
