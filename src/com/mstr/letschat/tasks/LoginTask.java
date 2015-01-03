package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.SmackHelper;

public class LoginTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	
	private String username;
	private String password;
	
	public LoginTask(Listener<Boolean> listener, Context context, String username, String password) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		
		this.username = username;
		this.password = password;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				SmackHelper smackHelper = SmackHelper.getInstance(context);
				
				smackHelper.login(username, password);
				
				UserUtils.setLoginUser(context, username, password, smackHelper.getLoginUserNickname());
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}