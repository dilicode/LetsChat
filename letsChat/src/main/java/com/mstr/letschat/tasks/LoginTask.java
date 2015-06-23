package com.mstr.letschat.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.SmackHelper;

public class LoginTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String username;
	private String password;

	private ProgressDialog dialog;
	
	public LoginTask(Listener<Boolean> listener, Context context, String username, String password) {
		super(listener, context);
		
		this.username = username;
		this.password = password;

		dialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.login));
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				SmackHelper smackHelper = SmackHelper.getInstance(context);
				
				smackHelper.login(username, password);
				
				UserUtils.setLoginUser(context, username, password, smackHelper.getLoginUserNickname());
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("login error %s", username), e);
				
				return Response.error(e);
			}
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Response<Boolean> response) {
		dialog.dismiss();

		super.onPostExecute(response);
	}

	@Override
	protected void onCancelled() {
		dialog.dismiss();
	}
}