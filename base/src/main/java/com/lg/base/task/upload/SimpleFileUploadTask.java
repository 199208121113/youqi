package com.lg.base.task.upload;

import com.lg.base.core.BaseRoboAsyncTask;
import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;

import java.io.File;
import java.util.Map;

import okhttp3.Response;

public class SimpleFileUploadTask extends BaseRoboAsyncTask<String> implements OnTaskRunningListener{

	final File uploadFile ;
	final String uploadUrl ;
	Map<String,String> params;
	public SimpleFileUploadTask(String uploadUrl, File uploadFile,Map<String,String> params) {
		super();
		this.uploadUrl = uploadUrl;
		this.uploadFile = uploadFile;
		this.params = params;
	}

	@Override
	public String run() throws Exception {
		if(uploadFile == null){
			throw new Exception("upload file can't be null");
		}
		if(!uploadFile.exists()){
			throw new Exception("file '"+uploadFile.getAbsolutePath()+"' not exists");
		}
		Response response = FileUploadTask.startUpload(uploadFile, params, null, this.uploadUrl, this);
		int respCode = response.code();
		if(respCode == 200){
			return uploadFile.getAbsolutePath();
		}
		throw new Exception("file upload failed,respCode="+respCode);
	}

	private volatile long lastSendTime = System.currentTimeMillis();
	@Override
	public final void sendProgressChanged(long handBytes, long totalBytes) {
		long tmp = System.currentTimeMillis();
		if(tmp-lastSendTime < 1000 && handBytes < totalBytes) {
			return;
		}
		lastSendTime = tmp;
		onProgressChanged(handBytes, totalBytes);
	}

	@Override
	public Status getTaskStatus() {
		return null;
	}

	protected void onProgressChanged(long handBytes, long totalBytes){

	}
}
