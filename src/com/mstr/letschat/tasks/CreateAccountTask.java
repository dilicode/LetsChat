package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.tasks.CreateAccountTask.AccountCreationResult;
import com.mstr.letschat.xmpp.XMPPHelper;

public class CreateAccountTask extends AsyncTask<Void, Void, AccountCreationResult> {
	public static enum AccountCreationResult {SUCCESS, FAILURE, CONFLICT};
	
	private WeakReference<AddAccountListener> listener;
	
	private String user;
	private String name;
	private String password;
	
	public static interface AddAccountListener {
		public void onAccountAdded(AccountCreationResult result);
	}
	
	public CreateAccountTask(AddAccountListener listener, String user, String name, String password) {
		this.listener = new WeakReference<AddAccountListener>(listener);
		
		this.user = user;
		this.name = name;
		this.password = password;
	}
	
	@Override
	public AccountCreationResult doInBackground(Void... params) {
		return XMPPHelper.getInstance().signup(user, name, password);
	}
	
	public void onPostExecute(AccountCreationResult result) {
		super.onPostExecute(result);
		
		AddAccountListener l = listener.get();
		if (l != null) {
			l.onAccountAdded(result);
		}
	}
}