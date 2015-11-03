package com.lg.base.core;

public class BaseException extends Throwable{

	private static final long serialVersionUID = 1L;

	public BaseException() {
		super();
	}

	public BaseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public BaseException(String detailMessage) {
		super(detailMessage);
	}

	public BaseException(Throwable throwable) {
		super(throwable);
	}
}
