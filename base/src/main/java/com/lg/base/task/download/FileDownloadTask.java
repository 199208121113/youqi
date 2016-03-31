package com.lg.base.task.download;

import android.os.OperationCanceledException;

import com.lg.base.bus.EventBus;
import com.lg.base.http.HttpConstant;
import com.lg.base.http.HttpMethod;
import com.lg.base.http.OKHttpUtil;
import com.lg.base.task.IWatcherCallback;
import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;
import com.lg.base.task.Task;
import com.lg.base.task.TaskEvent;
import com.lg.base.task.TaskService;
import com.lg.base.utils.IOUtil;
import com.lg.base.utils.MD5Util;
import com.lg.base.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SuppressWarnings("all")
public class FileDownloadTask extends Task<String> implements OnTaskRunningListener {

    @Override
    protected String doInBackground() throws Exception {
        final String downloadUrl = (String)getParams().get("download_url");
        final String lastSavePath = (String)getParams().get("save_path");

        //header参数
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpConstant.KEY_USER_AGENT, "Android_User_Agent");

        //body参数
        Map<String,String> bodyMap = new HashMap<>();
        startDownload(downloadUrl, bodyMap, lastSavePath, headerMap, this);
        return lastSavePath;
    }

    public static void startDownload(final String requestUrl, Map<String, String> params, final String fileSavePath,Map<String,String> headers,OnTaskRunningListener listener) throws Exception {
        final String tempSavePath = getTmpDataFileName(fileSavePath);
        if(headers == null){
            headers = new HashMap<>();
        }
        final int oldReceivedLength = Long.valueOf(IOUtil.getFileSize(tempSavePath)).intValue();
        if (oldReceivedLength > 0) {
            headers.put("Range", "bytes=" + oldReceivedLength + "-");
        }
        Request request = OKHttpUtil.buildRequest(requestUrl, HttpMethod.GET, params,headers);
        Response response = OKHttpUtil.execute(request, null);
        if (response == null)
            throw new Exception("http response is null");
        final int responseCode = response.code();
        long receivedLength = 0;
        FileOutputStream fos = null;
        if (responseCode == 200) {
            IOUtil.deleteByFilePath(tempSavePath);
            fos = new FileOutputStream(tempSavePath);
        } else if (responseCode == 206) {
            fos = new FileOutputStream(tempSavePath, true);
            receivedLength = Math.max(oldReceivedLength,0);
        } else {
            IOUtil.deleteByFilePath(tempSavePath);
        }

        if (fos == null)
            throw new Exception("fos is null,responseCode=" + responseCode + ",requestUrl=" + request.url().toString());
        if (StringUtil.isEmpty(fileSavePath))
            throw new IllegalArgumentException("lastSavePath is empty");
        ResponseBody rb = response.body();
        InputStream is = rb.byteStream();
        if (is == null) {
            throw new IllegalArgumentException("rb.byteStream() is null");
        }
        long contentLength = rb.contentLength();
        if (contentLength <= 0) {
            String lengthStr = response.header(HttpConstant.CONTENT_LENGTH,null);
            if (StringUtil.isNotEmpty(lengthStr)) {
                contentLength = Long.parseLong(lengthStr.trim());
            }
        }
        final long totalLenght = receivedLength + contentLength;
        byte[] byteBuffer = new byte[4096];
        try {
            boolean userCanceled = false;
            int n;
            while ((n = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                if(listener != null && (listener.getTaskStatus() == Status.CANCELED || listener.getTaskStatus() == Status.ERROR_STOPED) ){
                    userCanceled = true;
                    break;
                }
                fos.write(byteBuffer, 0, n);
                fos.flush();
                receivedLength += n;
                if(listener != null) {
                    listener.sendProgressChanged(receivedLength, totalLenght);
                }
            }
            if(listener != null) {
                listener.sendProgressChanged(receivedLength, totalLenght);
            }
            fos.close();
            fos = null;
            if (userCanceled) {
                throw new OperationCanceledException("requestUrl:[" + requestUrl + "] canceled");
            }
            if (totalLenght > 0 && receivedLength < totalLenght) {
                IOUtil.deleteByFilePath(fileSavePath);
                IOUtil.deleteByFilePath(tempSavePath);
                throw new Exception("Data is not complete,rec=[" + receivedLength + "/" + totalLenght + "]");
            }
            try {
                boolean deleted = IOUtil.deleteFileByRename(fileSavePath);
                if(!deleted) {
                    IOUtil.deleteByFilePath(fileSavePath);
                }
            } catch (Exception e) {
                //ignore this Exception
            }
            try {
                boolean renamed = new File(tempSavePath).renameTo(new File(fileSavePath));
                if(!renamed){
                   boolean copyed = IOUtil.copyFile(fileSavePath,tempSavePath);
                   if(!copyed){
                       throw new Exception("file copy failed");
                   }
                }
            } catch (Exception err) {
                IOUtil.deleteByFilePath(fileSavePath);
                throw err;
            }
        } finally {
            rb.close();
        }
    }

    private volatile long lastSendTime = System.currentTimeMillis();

    @Override
    public final void sendProgressChanged(long handBytes, long totalBytes) {
        long tmp = System.currentTimeMillis();
        if(tmp-lastSendTime < 1000 && handBytes < totalBytes) {
            return;
        }
        lastSendTime = tmp;

        Task.Progress pro = new Progress();
        pro.setCurrent(Long.valueOf(handBytes).intValue());
        pro.setTotal(Long.valueOf(totalBytes).intValue());
        if(totalBytes > 0) {
            final int scale = (int) (pro.getCurrent() * 100.0 / pro.getTotal());
            pro.setScale(scale);
        }
        setProgress(pro);
    }

    @Override
    public Status getTaskStatus() {
        return this.getStatus();
    }

    @SuppressWarnings("all")
    public static <T> void createTask(IWatcherCallback<T> watcher, String downloadUrl,String savePath) {
        if(downloadUrl == null || downloadUrl.trim().length() == 0)
            return;
        if(savePath == null || savePath.trim().length() == 0)
            return;
        TaskEvent te = new TaskEvent(TaskService.LOCATION);
        te.setTaskId(MD5Util.toMd5(downloadUrl));
        te.setTaskName(getFileName(savePath));
        te.setClazz(FileDownloadTask.class);
        te.setOperatorFlags(TaskEvent.Operate.FLAG_CREATE | TaskEvent.Operate.FLAG_START);
        if(watcher != null){
            te.setOperatorFlags(te.getOperatorFlags() | TaskEvent.Operate.FLAG_WATCH);
            te.setWatcher(watcher);
        }
        te.setTaskFlags(Task.FLAG_TIMELY_POOL);
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("download_url", downloadUrl);
        params.put("save_path", savePath);
        te.setParams(params);
        te.setMaxRetryCount(5);
        EventBus.get().sendEvent(te);
    }

    private static String getFileName(String path){
        if(path == null || path.trim().length() == 0)
            return "null";
        path = path.replaceAll("\\\\","/");
        final int pos = path.lastIndexOf("/");
        if(pos < 0){
            return path;
        }
        return path.substring(pos+1);
    }

    public static String getTmpDataFileName(final String savePath) throws Exception{
        if (savePath != null) {
            File pf = new File(savePath).getParentFile();
            if(pf != null && !pf.exists()){
                if(!pf.mkdirs()){
                    throw new Exception(pf.getAbsolutePath()+" can't created!");
                }
            }
        }
        return savePath + ".temp";
    }
}
