package com.lg.base.exception;

/**
 * 网络响应异常
 * Created by liguo on 2015/8/28.
 */
public class NetworkResponseException extends Exception {
    private int responseCode;

    public NetworkResponseException(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
