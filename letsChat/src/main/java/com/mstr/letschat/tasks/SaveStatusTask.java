package com.mstr.letschat.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.adapters.StatusListAdapter;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.SmackHelper;

import java.lang.ref.WeakReference;

public class SaveStatusTask extends BaseAsyncTask<Void, Void, Boolean> {
	private int position;
	private WeakReference<StatusListAdapter> adapterWrapper;
	private WeakReference<TextView> statusTextWrapper;
	
	private ProgressDialog dialog;
	
	public SaveStatusTask(Listener<Boolean> listener, Context context, StatusListAdapter adapter, TextView statusText, int position) {
		super(listener, context);
		
		adapterWrapper = new WeakReference<StatusListAdapter>(adapter);
		statusTextWrapper = new WeakReference<TextView>(statusText);
		this.position = position;
		
		dialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.update_status_text));
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		StatusListAdapter adapter = adapterWrapper.get();
		if (context != null && adapter != null) {
			try {
				SmackHelper.getInstance(context).saveStatus(adapter.getItem(position));
				
				return Response.success(true);
			} catch (SmackInvocationException e) {
				AppLog.e("set status error", e);
				return Response.error(e);
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Response<Boolean> response) {
		dismissDialog();
		
		Listener<Boolean> listener = getListener();
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				StatusListAdapter adapter = adapterWrapper.get();
				if (adapter != null) {
					adapter.setSelection(position);
					
					TextView statusText = statusTextWrapper.get();
					if (statusText != null) {
						statusText.setText(adapter.getItem(position));
					}
				}
			}
		}
		
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