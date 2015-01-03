package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class SignupTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	
	private String user;
	private String name;
	private String password;
	
	public SignupTask(Listener<Boolean> listener, Context context, String user, String password, String name) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		
		this.user = user;
		this.name = name;
		this.password = password;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				SmackHelper.getInstance(context).signupAndLogin(user, password, name);
				
				return Response.success(true); 
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		}
		
		return null;
	}
}