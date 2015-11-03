package com.lg.base.task;

import com.lg.base.task.Task.Progress;

public interface IWatcherCallback<Result> {
	void onSuccess(String taskId, Result result);
	void onError(String taskId, Throwable e);
	void onProgressUpdate(String taskId, Progress progress);
	void onCanceled(String taskId);
    void onCreated(Task t);
	void onStatusChanged(String taskId, Task t, Status newStatus, Status oldStatus);
	
	/** 1:只观察1个任务,2观察多个任务  */
	int getType();
	
	/** 是否失效 */
	boolean isDisabled();

	TaskType getTaskType();

}
