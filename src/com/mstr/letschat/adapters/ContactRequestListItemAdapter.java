package com.mstr.letschat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.ContactRequest;

public class ContactRequestListItemAdapter extends ResourceCursorAdapter {
	
	private OnAcceptButtonClickListener listener;
	
	public static interface OnAcceptButtonClickListener {
		public void onAcceptButtonClick(String origin, String nickname);
	}
	
	public ContactRequestListItemAdapter(Context context, Cursor c, int flags) {
		super(context, R.layout.contact_request_list_item, c, flags);
	}
	
	public OnAcceptButtonClickListener getListener() {
		return listener;
	}

	public void setListener(OnAcceptButtonClickListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		final String nickname = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME));
		final String origin = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_ORIGIN));
		
		viewHolder.userText.setText(nickname);
		viewHolder.messageText.setText(R.string.add_contact_text);
		
		int status = cursor.getInt(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_STATUS));
		if (status == ContactRequest.STATUS_ACCPTED) {
			viewHolder.acceptButton.setEnabled(false);
			viewHolder.acceptButton.setText(R.string.accepted);
		} else {
			viewHolder.acceptButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (listener != null) {
						listener.onAcceptButtonClick(origin, nickname);
					}
				}
			});
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.userText = (TextView)view.findViewById(R.id.tv_user);
		viewHolder.messageText = (TextView)view.findViewById(R.id.tv_message);
		viewHolder.acceptButton = (Button)view.findViewById(R.id.btn_accept);
		view.setTag(viewHolder);
		
		return view;
	 }
	
	static class ViewHolder {
		TextView userText;
		TextView messageText;
		Button acceptButton;
	}
}