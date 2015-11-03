package com.lg.base.exception;

/**
 * 网络异常
 * Created by liguo on 2015/8/28.
 */
public class NetworkRequestException extends Exception {
    public NetworkRequestException(Throwable throwable) {
        super(throwable);
    }
}
