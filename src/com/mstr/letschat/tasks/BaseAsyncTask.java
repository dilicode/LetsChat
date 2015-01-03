package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.tasks.Response.Listener;

public abstract class BaseAsyncTask<Params, Progress, T> extends AsyncTask<Params, Progress, Response<T>> {
	private WeakReference<Listener<T>> listenerWrapper;
	
	public BaseAsyncTask(Listener<T> listener) {
		this.listenerWrapper = new WeakReference<Listener<T>>(listener);
	}
	
	public Listener<T> getListener() {
		return listenerWrapper.get();
	}

	@Override
	protected void onPostExecute(Response<T> response) {
		Listener<T> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}