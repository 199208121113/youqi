package com.lg.base.task.download;
public interface TaskFilter<Result,Progress> {
	void onPreExecute();
	void onSuccess(Result result);
	void onException(Exception e);
	void onRunning(Progress prgress);
}
