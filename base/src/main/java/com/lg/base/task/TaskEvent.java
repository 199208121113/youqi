package com.lg.base.task;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

import java.io.Serializable;
import java.util.HashMap;

public class TaskEvent extends BaseEvent{
	
	private static final long serialVersionUID = 1L;
	private String taskId;
    private String taskName;
	private int taskFlags;
	private int operatorFlags;
	private HashMap<String,Serializable> params = null;
	private IWatcherCallback<?> watcher = null;
	private Class<?> clazz = null;
	

	public TaskEvent(Location from, Location to, Object data) {
		super(from, to, data);
	}

	public TaskEvent(Location from, Location to) {
		super(from, to);
	}

	public TaskEvent(Location to) {
		super(to);
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
	}

	public int getTaskFlags() {
		return taskFlags;
	}

	public void setTaskFlags(int taskFlags) {
		this.taskFlags = taskFlags;
	}

	public int getOperatorFlags() {
		return operatorFlags;
	}

	public void setOperatorFlags(int operatorFlags) {
		this.operatorFlags = operatorFlags;
	}

	public HashMap<String, Serializable> getParams() {
		return params;
	}

	public void setParams(HashMap<String, Serializable> params) {
		this.params = params;
	}

	public IWatcherCallback<?> getWatcher() {
		return watcher;
	}

	public void setWatcher(IWatcherCallback<?> watcher) {
		this.watcher = watcher;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public static class Operate {
		public static final int FLAG_CREATE = 1 << 1;
		public static final int FLAG_START = 1 << 2;
		public static final int FLAG_CANCEL = 1 << 3;
		public static final int FLAG_WATCH = 1 << 4;
		public static final int FLAG_START_FROM_FILE = 1 << 5;
	}
	
	private int maxRetryCount = 0;
	public void setMaxRetryCount(int maxRetryCount){
		this.maxRetryCount = maxRetryCount;
	}
	public int getMaxRetryCount() {
		return maxRetryCount;
	}
	
	//专为从文件启动任务时准备的
	private Task fromFileTask = null;
	public Task getFromFileTask() {
		return fromFileTask;
	}
	public void setFromFileTask(Task fromFileTask) {
		this.fromFileTask = fromFileTask;
	}

}
