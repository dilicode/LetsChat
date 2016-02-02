package com.mstr.letschat.tasks;

public class Response<T> {
	public interface Listener<T> {
		public void onResponse(T result);
		
		public void onErrorResponse(Exception exception);
	}
	
	public static <T> Response<T> success(T result) {
		return new Response<T>(result);
	}
	
	public static <T> Response<T> error(Exception e) {
		return new Response<T>(e);
	}
	
	private T result;
	
	private Exception exception;
	
	private Response(T result) {
		this.result = result;
	}
	
	private Response(Exception exception) {
		this.exception = exception;
	}
	
	public boolean isSuccess() {
		return exception == null;
	}

	public T getResult() {
		return result;
	}

	public Exception getException() {
		return exception;
	}
}