package com.lg.base.task.async;

import com.lg.base.core.BaseAsyncTask;
import com.lg.base.http.HttpConstant;
import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;
import com.lg.base.task.download.FileDownloadTask;
import com.lg.base.task.download.ProgressInfo;
import com.lg.base.task.download.TaskFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleFileDownloadTask extends BaseAsyncTask<String> implements OnTaskRunningListener {

	private static final String TAG = "SimpleFileDownloadTask";
	private String downloadUrl;
	private String fileSavePath;

	private List<TaskFilter<String,ProgressInfo>> taskFilters;
    private static final HashMap<String, String> downloadMap = new HashMap<>();
	public SimpleFileDownloadTask(String downloadUrl, String savePath) {
		this.downloadUrl = downloadUrl;
		this.fileSavePath = savePath;

	}
	public SimpleFileDownloadTask(String url, String savePath, TaskFilter<String, ProgressInfo> filter) {
		this(url,savePath);
		this.addTaskFilter(filter);
	}
	
	@Override
	public String run() throws Exception {
		if(isExistsByUrl(this.downloadUrl)){
			throw new Exception("已加入到下载列表");
		}
		downloadMap.put(downloadUrl, null);

		//header参数
		HashMap<String, String> headerMap = new HashMap<>();
		headerMap.put(HttpConstant.KEY_USER_AGENT, "Android_User_Agent");

		//body参数
		Map<String,String> bodyMap = new HashMap<>();
		FileDownloadTask.startDownload(this.downloadUrl, bodyMap, this.fileSavePath, headerMap, this);
		return fileSavePath;
	}

	public void addTaskFilter(TaskFilter<String,ProgressInfo> downloadFilter){
		if(taskFilters == null){
			taskFilters = new ArrayList<>();
		}
		taskFilters.add(downloadFilter);
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
		if(taskFilters != null && taskFilters.size() > 0){
			ProgressInfo pi = new ProgressInfo(totalBytes, handBytes);
			for (TaskFilter<String,ProgressInfo> filter : taskFilters) {
				filter.onRunning(pi);
			}
		}
	}

	@Override
	public Status getTaskStatus() {
		return Status.RUNNING;
	}

	public static boolean isExistsByUrl(String url){
		return downloadMap.containsKey(url);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(taskFilters == null)
			return ;
		for (TaskFilter<String,ProgressInfo> filter : taskFilters) {
			filter.onPreExecute();
		}
	}
	
	@Override
	protected void onSuccess(String t)  {
		super.onSuccess(t);
		if(taskFilters == null)
			return ;
		for (TaskFilter<String,ProgressInfo> filter : taskFilters) {
			filter.onSuccess(t);
		}
	}
	
	@Override
	protected void onException(Exception e) {
		super.onException(e);
		if(taskFilters == null)
			return ;
		for (TaskFilter<String,ProgressInfo> filter : taskFilters) {
			filter.onException(e);
		}
	}

    @Override
    protected void onFinally() {
        super.onFinally();
        downloadMap.remove(this.downloadUrl);
    }

	protected void onProgressChanged(long handBytes, long totalBytes){

	}

    public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getFileSavePath() {
		return fileSavePath;
	}
	
}
