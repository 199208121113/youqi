package com.lg.base.task.upload;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.OutputStream;

public class UpProgressOutHttpEntity extends HttpEntityWrapper {

    private final UpProgressListener uploadListener;
    private final long totalBytes;
    public UpProgressOutHttpEntity(final HttpEntity entity, final UpProgressListener listener,long totalBytes) {
        super(entity);
        this.uploadListener = listener;
        this.totalBytes = totalBytes;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        if(out instanceof UploadOutputStream){
            this.wrappedEntity.writeTo(out);
        }else{
            this.wrappedEntity.writeTo(new UploadOutputStream(out, this.uploadListener,this.totalBytes));
        }
    }
}