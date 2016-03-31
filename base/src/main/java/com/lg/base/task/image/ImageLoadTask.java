package com.lg.base.task.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.http.HttpMethod;
import com.lg.base.http.OKHttpUtil;
import com.lg.base.task.download.FileDownloadTask;
import com.lg.base.utils.IOUtil;
import com.lg.base.utils.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


@SuppressWarnings("javadoc")
public class ImageLoadTask extends BaseRoboAsyncTask<Bitmap> {

    String url = null;
    String savePath = null;
    OnImageLoadFinishCallBack callBack = null;
    Bundle param = new Bundle();
    public ImageLoadTask(Activity context,String url,String savePath) {
        super(context);
        this.url = url;
        this.savePath = savePath;
    }

    @Override
    public Bitmap run() throws Exception {
        int newWidth = getImageWidth();
        if(IOUtil.fileExist(savePath)){
            if(IOUtil.getFileSize(savePath) > 1024){
                Bitmap bmp = null;
                if (newWidth != -1) {
                    bmp = ImageUtil.getBitmapFromFile(savePath, newWidth);
                } else {
                    bmp = ImageUtil.getBitmap(savePath);
                }
                if (bmp == null) {
                	IOUtil.deleteByFilePath(savePath);
                    throw new Exception("can't load bitmap from file:" + savePath+" url="+this.url);
                }
                return bmp;
            }else{
                IOUtil.deleteByFilePath(savePath);
            }
        }
        Bitmap bmp = null;
        if(url != null && url.startsWith("file:")){
            String tmpUrl = url.trim().split(":")[1];
            File locFile = new File(tmpUrl);
            if(locFile.exists() && locFile.length() > 0){
                IOUtil.copyFile(savePath,locFile.getAbsolutePath());
            }
        }else{
            String tmpPath = FileDownloadTask.getTmpDataFileName(savePath);
            Request request = OKHttpUtil.buildRequest(url, HttpMethod.GET, null,null);
            Response response = OKHttpUtil.execute(request,null);
            if(response == null || !response.isSuccessful()){
                throw new Exception("下载失败");
            }
            ResponseBody rb = response.body();
            InputStream is = rb.byteStream();

            IOUtil.saveFileForInputStream(tmpPath,is);
            rb.close();
            IOUtil.rename(tmpPath, savePath);
        }

        if (newWidth != -1) {
            bmp = ImageUtil.getBitmapFromFile(savePath, newWidth);
        } else {
            bmp = ImageUtil.getBitmap(savePath);
        }
        if (bmp == null){
        	IOUtil.deleteByFilePath(savePath);
            throw new Exception("can't load bitmap from file:" + savePath);
        }
        return bmp;
    }

    @Override
    protected void onSuccess(Bitmap t) {
        super.onSuccess(t);
        if(this.callBack != null)
            this.callBack.onSuccess(t);
    }

    @Override
    protected void onException(Exception e) {
        super.onException(e);
        if(this.callBack != null)
            this.callBack.onException(e);
    }

    public void setCallBack(OnImageLoadFinishCallBack callBack) {
        this.callBack = callBack;
    }

    public void setImageWidth(int newWidth){
        param.putInt("width", newWidth);
    }
    public int getImageWidth(){
        return param.getInt("width", -1);
    }

    public interface OnImageLoadFinishCallBack{
        void onSuccess(Bitmap t);
        void onException(Exception e);
    }
    public String getUrl() {
        return url;
    }
    private String uuid;
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSavePath() {
		return savePath;
	}

	public Bundle getParam() {
		return param;
	}

}
