package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.xmpp.XMPPHelper;

public class LoginTask extends AsyncTask<Void, Void, Boolean> {
	
	public static interface LoginListener {
		public void onLogin(boolean result);
	}
	
	private String username;
	private String password;
	
	private WeakReference<LoginListener> listener;
	
	public LoginTask(LoginListener listener, String username, String password) {
		this.listener = new WeakReference<LoginListener>(listener);
		
		this.username = username;
		this.password = password;
	}
	
	@Override
	public Boolean doInBackground(Void... params) {
		return XMPPHelper.getInstance().connectAndLogin(username, password);
	}
	
	@Override
	public void onPostExecute(Boolean result) {
		LoginListener l = listener.get();
		
		if (l != null) {
			l.onLogin(result);
		}
	}
}