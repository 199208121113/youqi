package com.lg.base.http;

import com.lg.base.core.LogUtil;
import com.lg.base.http.resp.DefaultHandler;
import com.lg.base.http.resp.HttpResponseHandler;
import com.lg.base.utils.GsonUtil;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class HttpService {

    protected final String tag = this.getClass().getSimpleName();

    protected <T> T get(String url, Type resultType) throws Exception {
        return get(url, null, resultType);
    }

    protected <T> T get(String url, Map<String, String> params, Type resultType) throws Exception {
        return get(url, params, resultType, null);
    }

    protected <T> T get(String url, Map<String, String> params, Type resultType, Map<String, String> headers) throws Exception {
        return get(url, params, resultType, headers, null);
    }

    protected <T> T get(String url, Map<String, String> params, Type resultType, Map<String, String> headers, HttpResponseHandler respHandler) throws Exception {
        Map<String, String> paramsNew = getBasicParams();
        if (params != null && params.size() > 0) {
            paramsNew.putAll(params);
        }
        Map<String, String> headersNew = getBasicHeaders();
        if (headersNew != null && headersNew.size() > 0) {
            headersNew.putAll(headers);
        }
        Request request = OKHttpUtil.buildRequest(url, HttpMethod.GET, paramsNew, headersNew);
        LogUtil.d(tag, "get,url=" + request.urlString());
        Response response = OKHttpUtil.execute(request, null);
        if (respHandler == null)
            respHandler = DefaultHandler.getDefaultInstance();
        return respHandler.handResposne(response, resultType);
    }

    protected <T> T post(String url, Map<String, String> params, Type resultType) throws Exception {
        return post(url, params, resultType, null);
    }

    protected <T> T post(String url, Map<String, String> params, Type resultType, Map<String, String> headers) throws Exception {
        return post(url, params, resultType, headers, null);
    }

    protected <T> T post(String url, Map<String, String> params, Type resultType, Map<String, String> headers, HttpResponseHandler respHandler) throws Exception {
        Map<String, String> paramsNew = getBasicParams();
        if (params != null && params.size() > 0) {
            paramsNew.putAll(params);
        }
        Map<String, String> headersNew = getBasicHeaders();
        if (headersNew != null && headersNew.size() > 0) {
            headersNew.putAll(headers);
        }
        LogUtil.d(tag, "post,url=" + url + " param=" + GsonUtil.getGson().toJson(paramsNew));
        Request request = OKHttpUtil.buildRequest(url, HttpMethod.POST, paramsNew, headersNew);
        Response response = OKHttpUtil.execute(request, null);
        if (respHandler == null)
            respHandler = DefaultHandler.getDefaultInstance();
        return respHandler.handResposne(response, resultType);
    }

    public static final String USER_AGENT = "Android-UserAgent";

    public static Map<String, String> getBasicParams() {
        Map<String, String> pm = new HashMap<>();
        //公共参数
        pm.put("key", "value");
        return pm;
    }

    public static Map<String, String> getBasicHeaders() {
        Map<String, String> pm = new HashMap<>();
        //公共参数
        pm.put(HttpConstant.KEY_USER_AGENT, USER_AGENT);
        return pm;
    }
}
