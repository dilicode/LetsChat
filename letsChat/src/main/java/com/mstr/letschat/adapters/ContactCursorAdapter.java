package com.mstr.letschat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.bitmapcache.AvatarImageFetcher;
import com.mstr.letschat.databases.ChatContract.ContactTable;

public class ContactCursorAdapter extends ResourceCursorAdapter {
	private AvatarImageFetcher imageFetcher;
	
	public ContactCursorAdapter(Context context, Cursor c, AvatarImageFetcher imageFetcher) {
		super(context, R.layout.contact_list_item, c, 0);
		
		this.imageFetcher = imageFetcher;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		imageFetcher.loadImage(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_JID)), viewHolder.avatar);
		viewHolder.nameText.setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME)));
		viewHolder.statusText.setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_STATUS)));
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.nameText = (TextView)view.findViewById(R.id.tv_nickname);
		viewHolder.statusText = (TextView)view.findViewById(R.id.tv_status);
		viewHolder.avatar = (ImageView)view.findViewById(R.id.avatar);
		view.setTag(viewHolder);
		
		return view;
	 }
	
	static class ViewHolder {
		TextView nameText;
		TextView statusText;
		ImageView avatar;
	}
}