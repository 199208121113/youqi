package com.lg.base.task.upload;

/**
 * 文件上传进度监听器接口
 */
public interface UpProgressListener {
	void transferred(long uploadedBytes,long totalBytes);
}
