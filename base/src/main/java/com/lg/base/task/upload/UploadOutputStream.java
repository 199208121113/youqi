package com.lg.base.task.upload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by liguo on 2015/10/19.
 */
public class UploadOutputStream extends FilterOutputStream {
    private final UpProgressListener tmpListener;
    private final long totalBytes;
    private long uploadedBytes = 0;
    public UploadOutputStream(final OutputStream out,final UpProgressListener listener,final long totalBytes) {
        super(out);
        this.tmpListener = listener;
        this.totalBytes = totalBytes;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        // NO, double-counting, as super.write(byte[], int, int)
        // delegates to write(int).
        // super.write(b, off, len);
        out.write(b, off, len);
        this.uploadedBytes += len;
        if(tmpListener != null) {
            this.tmpListener.transferred(this.uploadedBytes, totalBytes);
        }
    }

    @Override
    public void write(final int b) throws IOException {
        out.write(b);
        this.uploadedBytes++;
        if(tmpListener != null) {
            this.tmpListener.transferred(this.uploadedBytes, totalBytes);
        }
    }
}
