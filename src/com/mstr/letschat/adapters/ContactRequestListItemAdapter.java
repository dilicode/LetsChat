package com.mstr.letschat.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.utils.DatabaseUtils;

public class ContactRequestListItemAdapter extends ResourceCursorAdapter {
	
	private OnAcceptButtonClickListener onAcceptButtonClicklistener;
	
	public static interface OnAcceptButtonClickListener {
		public void onAcceptButtonClick(Uri uri);
	}
	
	public ContactRequestListItemAdapter(Context context, Cursor c, int flags) {
		super(context, R.layout.contact_request_list_item, c, flags);
	}
	
	public OnAcceptButtonClickListener getOnAcceptButtonClicklistener() {
		return onAcceptButtonClicklistener;
	}

	public void setOnAcceptButtonClicklistener(
			OnAcceptButtonClickListener onAcceptButtonClicklistener) {
		this.onAcceptButtonClicklistener = onAcceptButtonClicklistener;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		viewHolder.userText.setText(cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME)));
		viewHolder.messageText.setText(R.string.add_contact_text);
		
		int status = cursor.getInt(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_STATUS));
		final int id = cursor.getInt(cursor.getColumnIndex(ContactRequestTable._ID));
		if (status == DatabaseUtils.CONTACT_REQUEST_STATUS_ACCPTED) {
			viewHolder.acceptButton.setEnabled(false);
			viewHolder.acceptButton.setText(R.string.added);
		} else {
			viewHolder.acceptButton.setEnabled(true);
			viewHolder.acceptButton.setText(R.string.accept);
			viewHolder.acceptButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (onAcceptButtonClicklistener != null) {
						onAcceptButtonClicklistener.onAcceptButtonClick(ContentUris.withAppendedId(ContactRequestTable.CONTENT_URI, id));
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