package com.lg.base.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.lg.base.exception.NetworkRequestException;
import com.lg.base.utils.GsonUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class OKHttpUtil {

    /** 连接超时时间 */
    public static final int HAND_SHAKE_TIMEOUT_MILLIS = 1000 * 50;

    private static OkHttpClient okHttpClient = null;

    @SuppressWarnings("unchecked")
    public static <T> T execute(Request request,Type resultType) throws Exception{
        Response response;
        try {
            response = getOkHttpClient().newCall(request).execute();
        } catch (Exception e) {
            throw new NetworkRequestException(e);
        }
        if (resultType == null || resultType == Response.class) {
            return (T) response;
        }
        if (resultType == Void.TYPE) {
            return null;
        }
        if(resultType == String.class){
            return (T)response.body().string();
        }
        if(resultType == Bitmap.class){
            byte[] bytes = response.body().bytes();
            return (T)BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return GsonUtil.getGson().fromJson(response.body().charStream(), resultType);
    }

    public static Request buildRequest(String url,HttpMethod method,Map<String,String> params,Map<String,String> headers){
        Request.Builder builder;
        if(method == HttpMethod.GET){
            builder = buildRequestByGet(url,params);
        }else if(method == HttpMethod.POST){
            builder = buildRequestByPost(url, params);
        }else{
            throw new RuntimeException(method.name()+" is Supported ?");
        }
        if(headers != null && headers.size() > 0){
            for (String name : headers.keySet()) {
                builder.addHeader(name, headers.get(name));
            }
        }
        return builder.build();
    }

    private static OkHttpClient getOkHttpClient(){
        System.setProperty("http.keepAlive","false");
        if(okHttpClient != null) {
            return okHttpClient;
        }
        okHttpClient = new OkHttpClient();
        okHttpClient.setFollowRedirects(true);
        okHttpClient.setFollowSslRedirects(true);
        okHttpClient.setRetryOnConnectionFailure(false);
        okHttpClient.setConnectTimeout(50, TimeUnit.SECONDS);

        setSSLSocketFactory2();
        okHttpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return okHttpClient;
    }
    private static void setSSLSocketFactory1(){
        SSLSocketFactory ssf = SSLCertificateSocketFactory.getDefault(HAND_SHAKE_TIMEOUT_MILLIS,null);
        okHttpClient.setSslSocketFactory(ssf);
    }

    private static void setSSLSocketFactory2(){
		/*final TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}
		};*/
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            //sslContext.init(null, trustAllCerts, new SecureRandom());
            sslContext.init(null, null, null);
            final SSLSocketFactory ssf = sslContext.getSocketFactory();
            if(ssf != null){
                okHttpClient.setSslSocketFactory(ssf);
            }
        }  catch (Exception e) {
            setSSLSocketFactory1();
        }
    }

    private static Request.Builder buildRequestByGet(String url,Map<String,String> params){
        StringBuilder sb = new StringBuilder(url);
        if(params != null && params.size() > 0){
            Uri uri = Uri.parse(url);
            String query = uri.getQuery();
            boolean hasQuery = false;
            if(query != null && query.trim().length() > 0){
                hasQuery=true;
            }
            for (String key : params.keySet()) {
                if(hasQuery){
                    sb.append("&");
                }else{
                    sb.append("?");
                    hasQuery=true;
                }
                String value = params.get(key);
                if(TextUtils.isEmpty(value))
                    continue;
                try {
                    value = URLEncoder.encode(value,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(key).append("=").append(value);
            }
        }
        return new Request.Builder().url(sb.toString());
    }

    private static Request.Builder buildRequestByPost(final String url,Map<String,String> params){
        return new Request.Builder().url(url).post(getFormRequestBody(params));
    }

    public static RequestBody getFormRequestBody(Map<String,String> params){
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        if(params != null && params.size() > 0){
            String value;
            for (String key : params.keySet()) {
                value = params.get(key);
                if(TextUtils.isEmpty(value)){
                    continue;
                }
                try {
                    bodyBuilder.add(key,value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bodyBuilder.build();
    }
}