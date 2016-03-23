package com.lg.base.task.upload;

import com.lg.base.core.EventBus;
import com.lg.base.http.OKHttpUtil;
import com.lg.base.task.IWatcherCallback;
import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;
import com.lg.base.task.Task;
import com.lg.base.task.TaskEvent;
import com.lg.base.task.TaskService;
import com.lg.base.utils.MD5Util;
import com.lg.base.utils.StringUtil;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings("all")
public class FileUploadTask extends Task<String> implements OnTaskRunningListener {
    private static final String KEY_UPLOAD_FILE_PATH = "upload_file_path";
    private static final String KEY_UPLOAD_URL = "KEY_UPLOAD_URL";

    @Override
    protected String doInBackground() throws Exception {
        final String uploadFilePath = (String)getParams().get(KEY_UPLOAD_FILE_PATH);
        final String uploadUrl = (String)getParams().get(KEY_UPLOAD_URL);

        File file = new File(uploadFilePath);

        HashMap<String, Serializable> params = getParams();
        Map<String,String> newParams = new HashMap<>();
        for (String key : params.keySet()){
            if(KEY_UPLOAD_FILE_PATH.equals(key) || KEY_UPLOAD_URL.equals(key)){
                continue;
            }
            if(StringUtil.isEmpty(key)){
                continue;
            }
            String value = (String)params.get(key);
            if(StringUtil.isEmpty(value)){
                continue;
            }
            newParams.put(key, value);
        }
        Response response = startUpload(file, newParams, null,uploadUrl,this);
        if(!response.isSuccessful()){
            throw new Exception("file upload failed,respCode="+response.code());
        }
        return uploadFilePath;
    }

    public static <T> void createTask(IWatcherCallback<T> watcher,String uploadUrl,File uploadFile,Map<String,String> otherParams) {
        if(StringUtil.isEmpty(uploadUrl))
            return;
        TaskEvent te = new TaskEvent(TaskService.LOCATION);
        te.setTaskId(MD5Util.toMd5(uploadFile.getAbsolutePath()));
        te.setTaskName(uploadFile.getName());
        te.setClazz(FileUploadTask.class);
        te.setOperatorFlags(TaskEvent.Operate.FLAG_CREATE | TaskEvent.Operate.FLAG_START);
        if(watcher != null){
            te.setOperatorFlags(te.getOperatorFlags()|TaskEvent.Operate.FLAG_WATCH);
            te.setWatcher(watcher);
        }
        te.setTaskFlags(Task.FLAG_TIMELY_POOL);
        HashMap<String, Serializable> params = new HashMap<>();
        params.put(KEY_UPLOAD_FILE_PATH, uploadFile.getAbsolutePath());
        params.put(KEY_UPLOAD_URL,uploadUrl);
        if(otherParams != null && otherParams.size() > 0){
            for (String key : otherParams.keySet()){
                params.put(key, otherParams.get(key));
            }
        }
        te.setParams(params);
        te.setMaxRetryCount(2);
        EventBus.get().sendEvent(te);
    }

    public static Response startUpload(File file,Map<String,String> paramsNew,Map<String,String> headers,String uploadUrl,OnTaskRunningListener listener) throws Exception{
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        builder.addFormDataPart("fileData", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"),file) );

        for (String key : paramsNew.keySet()){
            builder.addFormDataPart(key,paramsNew.get(key));
        }

        Request.Builder requestBuilder = new Request.Builder().url(uploadUrl).post(new FileUploadRequestBody(builder.build(),listener));
        if(headers != null && headers.size() > 0) {
            for (String name : headers.keySet()) {
                requestBuilder.addHeader(name, headers.get(name));
            }
        }
        Request request = requestBuilder.build();
        Response response = OKHttpUtil.execute(request, null);
        return response;
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
        pro.setTaskId(this.getId());
        pro.setTaskType(this.getTaskType());
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
}
