package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.tasks.AddAccountTask.AccountCreationResult;
import com.mstr.letschat.utils.XMPPUtils;

public class AddAccountTask extends AsyncTask<Void, Void, AccountCreationResult> {
	public static enum AccountCreationResult {SUCCESS, FAILURE, CONFLICT};
	
	private WeakReference<AddAccountListener> listener;
	
	private String name;
	private String password;
	
	public static interface AddAccountListener {
		public void onAccountAdded(AccountCreationResult result);
	}
	
	public AddAccountTask(AddAccountListener listener, String name, String password) {
		this.listener = new WeakReference<AddAccountListener>(listener);
		
		this.name = name;
		this.password = password;
	}
	
	@Override
	public AccountCreationResult doInBackground(Void... params) {
		return XMPPUtils.addAccount(name, password);
	}
	
	public void onPostExecute(AccountCreationResult result) {
		super.onPostExecute(result);
		
		AddAccountListener l = listener.get();
		if (l != null) {
			l.onAccountAdded(result);
		}
	}
}