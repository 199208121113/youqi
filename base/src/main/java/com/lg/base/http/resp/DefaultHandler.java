package com.lg.base.http.resp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lg.base.exception.NetworkRequestException;
import com.lg.base.exception.NetworkResponseException;
import com.lg.base.utils.GsonUtil;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * HttpResponse的默认处理器
 *
 * @author liguo
 */
public class DefaultHandler implements HttpResponseHandler {

    private static DefaultHandler instance = null;

    protected DefaultHandler() {
        super();
    }

    public static DefaultHandler getDefaultInstance() {
        if (instance == null) {
            instance = new DefaultHandler();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T handResposne(Response response, Type resultType) throws Exception {
        if (resultType == null || resultType == HttpResponse.class || response == null) {
            return (T) response;
        }
        if (resultType == Void.TYPE) {
            return null;
        }
        ResponseBody responseBody = response.body();
        if(responseBody == null){
            return null;
        }
        T t = null;
        final int code = response.code();
        if(!(response.isSuccessful() || code == 302)){
            throw new NetworkResponseException(code);
        }
        InputStream is = null;
        Reader reader = null;
        try {
            if (code == 200) {
                if (resultType == Bitmap.class) {
                    is = responseBody.byteStream();
                    t = (T) BitmapFactory.decodeStream(is);
                } else if (resultType == String.class) {
                    String jsonStr = responseBody.string();
                    t = (T) jsonStr;
                } else {
                    reader = responseBody.charStream();
                    t = GsonUtil.getGson().fromJson(reader, resultType);
                }
            }
        } catch (IOException e){
            throw new NetworkRequestException(e);
        } catch (Exception e){
            throw e;
        } finally {
            closeInputStreamReader(reader);
            closeInputStream(is);
            try {
                responseBody.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    protected void closeInputStream(InputStream is) {
        if (is == null)
            return;
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeInputStreamReader(Reader reader) {
        if (reader == null)
            return;
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
