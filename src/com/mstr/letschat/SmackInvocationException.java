package com.mstr.letschat;

@SuppressWarnings("serial")
public class SmackInvocationException extends Exception {
	public SmackInvocationException(String detailMessage) {
		super(detailMessage);
	}
	
	public SmackInvocationException(Throwable throwable) {
		super(throwable);
	}
	
	public SmackInvocationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}