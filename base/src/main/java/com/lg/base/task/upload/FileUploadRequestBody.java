package com.lg.base.task.upload;

import com.lg.base.task.OnTaskRunningListener;
import com.lg.base.task.Status;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 文件上传的ReestBody
 * Created by liguo on 2015/9/21.
 */
public class FileUploadRequestBody extends RequestBody {

    //实际的待包装请求体
    private final RequestBody requestBody;

    //进度回调接口
    private final OnTaskRunningListener listener;

    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    /**
     * @param requestBody 待包装的请求体
     * @param listener 回调接口
     */
    public FileUploadRequestBody(RequestBody requestBody, OnTaskRunningListener listener) {
        this.requestBody = requestBody;
        this.listener = listener;
    }

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    /**
     * 重写进行写入
     *
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            //包装
            bufferedSink = Okio.buffer(toSink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();

    }

    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private Sink toSink(Sink sink) {
        long contentLenght = 0;
        try {
            contentLenght = contentLength();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MySink(sink,contentLenght,listener);
    }

    public static class MySink extends ForwardingSink{
        private final long contentLength;
        private final OnTaskRunningListener listener;
        boolean userCanceled = false;
        public MySink(Sink delegate,long contentLength,OnTaskRunningListener uploadListener) {
            super(delegate);
            this.contentLength = contentLength;
            this.listener  = uploadListener;
            this.userCanceled = false;
        }

        private long bytesWritten = 0L;

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            if(listener != null) {
                listener.sendProgressChanged(bytesWritten, contentLength);
                if(listener.getTaskStatus() == Status.CANCELED || listener.getTaskStatus() == Status.ERROR_STOPED){
                    userCanceled = true;
                    throw new IOException(OnTaskRunningListener.OPERATION_CANCELED_FLAG);
                }
            }
        }
    }
}
