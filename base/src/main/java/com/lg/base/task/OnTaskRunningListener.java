package com.lg.base.task;

/**
 * Created by liguo on 2015/10/20.
 */
public interface OnTaskRunningListener {
    void sendProgressChanged(long handBytes, long totalBytes);
    Status getTaskStatus();
    String OPERATION_CANCELED_FLAG = "OPERATION_CANCELED_FLAG";
}
