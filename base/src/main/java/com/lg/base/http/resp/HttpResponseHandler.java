package com.lg.base.http.resp;

import com.squareup.okhttp.Response;

import java.lang.reflect.Type;

public interface HttpResponseHandler {
	
	/**
	 * 将resp经过处理之后，返回resultType类型
	 * @param resp http请求的响应结果
	 * @param resultType 返回的结果类型，与返回类型T是一致的
	 */
	public <T> T handResposne(Response resp, Type resultType) throws Exception;
}
