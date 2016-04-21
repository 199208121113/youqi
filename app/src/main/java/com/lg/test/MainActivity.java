package com.lg.test;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventThread;
import com.lg.base.bus.LogUtil;
import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.InjectView;
import com.lg.base.task.async.SimpleFileDownloadTask;
import com.lg.base.task.async.SimpleFileUploadTask;
import com.lg.base.ui.recycle.RecyclerViewAdapter;
import com.lg.base.ui.recycle.RecyclerViewHolder;
import com.lg.base.utils.DateUtil;
import com.lg.base.utils.IOUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.account.CollectTask;
import com.lg.test.activity.TestDbActivity;
import com.lg.test.activity.TestEncodeActivity;
import com.lg.test.activity.TestQrCodeActivity;
import com.lg.test.activity.TestRecyclerViewActivity;
import com.lg.test.core.SuperActivity;
import com.lg.test.model.SimpleRow;
import com.zhy.changeskin.SkinManager;
import com.zhy.changeskin.callback.ISkinChangingCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.iwgang.familiarrecyclerview.FamiliarRecyclerView;


public class MainActivity extends SuperActivity implements FamiliarRecyclerView.OnItemClickListener {

    @InjectView(R.id.act_main_rv)
    FamiliarRecyclerView frv;

    MainAdapter mainAdapter;
    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("app demo");
    }

    @Override
    protected int getDefaultLeftIcon() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frv.setOnItemClickListener(this);
        mainAdapter = new MainAdapter(this);
        initAdapterData();
        frv.setAdapter(mainAdapter);
        mainAdapter.notifyDataSetChanged();
    }

    private void initAdapterData(){
        mainAdapter.addItem(new SimpleRow(1,"Account"),null);
        mainAdapter.addItem(new SimpleRow(2,"DataBase"),null);
        mainAdapter.addItem(new SimpleRow(3,"FileUpload"),null);
        mainAdapter.addItem(new SimpleRow(4,"FileDownload"),null);
        mainAdapter.addItem(new SimpleRow(5,"ScanQrCode"),null);
        mainAdapter.addItem(new SimpleRow(6,"Encode"),null);
        mainAdapter.addItem(new SimpleRow(7,"RecyclerView"),null);
        mainAdapter.addItem(new SimpleRow(8,"change to green"),null);
        mainAdapter.addItem(new SimpleRow(9,"change to default"),null);
        mainAdapter.addItem(new SimpleRow(10,"change to orange(apk plugin)"),null);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    private static final int WHAT_RATE = 1;
    Future rateFuture;
    @Override
    public void onItemClick(FamiliarRecyclerView familiarRecyclerView, View view, int position) {
        int v = mainAdapter.getItem(position).getData().getId();
        if(v == 1){
            collect();
        }else if(v == 2){
            startActivity(TestDbActivity.createIntent(this));
        }else if(v == 3){
            upload();
        }else if(v == 4){
            download();
        }else if(v == 5){
            startActivity(TestQrCodeActivity.createIntent(this));
        }else if(v == 6){
            startActivity(TestEncodeActivity.createIntent(this));
        }else if(v == 7){
            startActivity(TestRecyclerViewActivity.createIntent(this));
            if(rateFuture != null && !rateFuture.isDone() && !rateFuture.isCancelled()){
                rateFuture.cancel(true);
            }
            BaseEvent evt = new BaseEvent(getLocation(),WHAT_RATE).setRunOnThread(EventThread.UI);
            rateFuture = EventBus.get().sendEvent(evt,0, 5, TimeUnit.SECONDS);
        }else if(v == 8){
            SkinManager.getInstance().changeSkin("green");
        }else if(v == 9){
            SkinManager.getInstance().changeSkin("");
        }else if(v == 10){
            String skinPlugPath = IOUtil.getExternalStoragePath()+"skin_orange.apk";
            SkinManager.getInstance().changeSkin(skinPlugPath, "com.skin.orange", "orange", new ISkinChangingCallback() {
                @Override
                public void onStart() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    ToastUtil.show("切换成功");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rateFuture != null){
            if(!(rateFuture.isDone() || rateFuture.isCancelled())){
                rateFuture.cancel(true);
            }
        }
    }

    @Override
    public void executeMessage(Message msg) {
        super.executeMessage(msg);
        ToastUtil.show("executeMessage(),msg.what=" + msg.what);
    }

    @Override
    public void executeEvent(BaseEvent evt) {
        super.executeEvent(evt);
        if(evt.getWhat() == WHAT_RATE) {
            String time = DateUtil.formatDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
            LogUtil.d(TAG, "executeEvent(),time=" + time + ",tid=" + Thread.currentThread().getId());
        }
    }

    /** 收藏 */
    private  void collect(){
        new CollectTask(this){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ToastUtil.show("account.name=" + s);
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
        new SimpleFileUploadTask(uploadLoadUrl,uploadFile,params){
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ToastUtil.show("上传成功");
            }

            @Override
            protected void onProgressChanged(long handBytes, long totalBytes) {
                LogUtil.e(TAG, "uploadedBytes=" + handBytes + ",totalBytes=" + totalBytes);
            }
        }.execute();
    }

    private void download(){
        String url = "http://andfls.qiniudn.com/AIReader.apk";
        String pp = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AIReader.apk";
        new SimpleFileDownloadTask(url,pp){
            @Override
            protected void onSuccess(String t) {
                super.onSuccess(t);
                ToastUtil.show("下载成功");
            }

            @Override
            public void onProgressChanged(long handBytes, long totalBytes) {
                LogUtil.e(TAG, "handBytes=" + handBytes + ",totalBytes=" + totalBytes);
            }
        }.execute();
    }

    public static Map<String,String> getGenericParams(){
        Map<String,String> pm = new HashMap<>();
        pm.put("deviceID","5ecc403e012bf23b391146f0438b5863");
        pm.put("passID","books by AireaderCity_1234567890");
        pm.put("ver","4.65");
        pm.put("clientVersion","5.2");
        pm.put("appID","com.youloft.glsc");
        pm.put("appPackageName","com.ireadercity");
        pm.put("deviceType","Android");
        return pm;
    }

    private static class MainAdapter extends RecyclerViewAdapter<SimpleRow,Void,RecyclerViewHolder<SimpleRow,Void>> {
        public MainAdapter(Context ctx) {
            super(ctx);
        }

        @Override
        public RecyclerViewHolder<SimpleRow, Void> onCreateViewHolder(ViewGroup parent, int viewType) {
            View vv = inflater.inflate(R.layout.item_simple_row,parent,false);
            MainHolder holder = new MainHolder(vv,getCtx());
            holder.initViews();
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder<SimpleRow, Void> holder, int position) {
            holder.setItem(getItem(position));
        }
    }

    private static class MainHolder extends RecyclerViewHolder<SimpleRow,Void>{
        public MainHolder(View rootView, Context ctx) {
            super(rootView, ctx);
        }
        TextView tv;
        @Override
        protected void onInitViews(View view) {
            tv = find(R.id.item_simple_row_tv);
        }

        @Override
        protected void onBindItem() {
            bindText();
        }

        @Override
        protected void onRecycleItem() {

        }

        @Override
        protected void onRefreshView() {
            bindText();
        }

        private void bindText(){
            SimpleRow sr = getItem().getData();
            tv.setText(sr.getText());
        }

        @Override
        protected void onDestroy() {
            tv = null;
        }
    }
}
