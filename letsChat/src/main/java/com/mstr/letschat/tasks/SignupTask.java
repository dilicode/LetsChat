package com.mstr.letschat.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.BitmapFactory;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.bitmapcache.ImageCache;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.utils.PreferenceUtils;
import com.mstr.letschat.xmpp.SmackHelper;

public class SignupTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String user;
	private String name;
	private String password;
	private byte[] avatar;

	private ProgressDialog dialog;
	
	public SignupTask(Listener<Boolean> listener, Context context, String user, String password, String name, byte[] avatar) {
		super(listener, context);
		
		this.user = user;
		this.name = name;
		this.password = password;
		this.avatar = avatar;

		dialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.signup));
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				SmackHelper.getInstance(context).signupAndLogin(user, password, name, avatar);
				
				if (avatar != null) {
					ImageCache.addAvatarToFile(context, user, BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
				}
				
				PreferenceUtils.setLoginUser(context, user, password, name);
				
				return Response.success(true); 
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("sign up error %s", e.toString()), e);
				
				return Response.error(e);
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Response<Boolean> response) {
		dismissDialog();

		super.onPostExecute(response);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		dismissDialog();
	}

	public void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public void dismissDialogAndCancel() {
		dismissDialog();
		cancel(false);
	}
}