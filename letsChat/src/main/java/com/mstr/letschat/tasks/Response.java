package com.mstr.letschat.tasks;

import com.mstr.letschat.SmackInvocationException;

public class Response<T> {
	
	public interface Listener<T> {
		public void onResponse(T result);
		
		public void onErrorResponse(SmackInvocationException exception);
	}
	
	public static <T> Response<T> success(T result) {
		return new Response<T>(result);
	}
	
	public static <T> Response<T> error(SmackInvocationException e) {
		return new Response<T>(e);
	}
	
	private T result;
	
	private SmackInvocationException exception;
	
	private Response(T result) {
		this.result = result;
	}
	
	private Response(SmackInvocationException exception) {
		this.exception = exception;
	}
	
	public boolean isSuccess() {
		return exception == null;
	}

	public T getResult() {
		return result;
	}

	public SmackInvocationException getException() {
		return exception;
	}
}