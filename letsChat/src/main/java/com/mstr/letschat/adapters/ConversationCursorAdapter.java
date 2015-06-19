package com.mstr.letschat.adapters;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.ConversationActivity;
import com.mstr.letschat.R;
import com.mstr.letschat.bitmapcache.ImageFetcher;
import com.mstr.letschat.databases.ChatContract.ConversationTable;

public class ConversationCursorAdapter extends ResourceCursorAdapter {
	private int primaryTextColor;
	private int subTextColor;
	private DateFormat dateFormat;
	
	private ImageFetcher imageFetcher;
	
	public ConversationCursorAdapter(ConversationActivity context, Cursor c, int flags) {
		super(context, R.layout.conversation_list_item, c, flags);
		
		TypedArray a = context.obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
		primaryTextColor = a.getColor(0, 0);
		a.recycle();
		
		subTextColor = context.getResources().getColor(R.color.sub_text_color);
		
		dateFormat = DateFormat.getDateInstance();
		
		imageFetcher = context.getImageFetcher();
	}
	
	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		imageFetcher.loadImage(cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_NAME)), viewHolder.avatar);
		viewHolder.nameText.setText(cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_NICKNAME)));
		viewHolder.messageText.setText(cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_LATEST_MESSAGE)));
		viewHolder.dateText.setText(dateFormat.format(
				new Date(cursor.getLong(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_TIME)))));
		
		int unreadCount = cursor.getInt(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_UNREAD));
		if (unreadCount == 0) {
			viewHolder.unreadCountText.setVisibility(View.GONE);
			viewHolder.dateText.setTextColor(subTextColor);
			viewHolder.messageText.setTextColor(subTextColor);
		} else {
			viewHolder.unreadCountText.setText(String.valueOf(unreadCount));
			viewHolder.unreadCountText.setVisibility(View.VISIBLE);
			viewHolder.dateText.setTextColor(primaryTextColor);
			viewHolder.messageText.setTextColor(primaryTextColor);
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.nameText = (TextView)view.findViewById(R.id.tv_nickname);
		viewHolder.messageText = (TextView)view.findViewById(R.id.tv_message);
		viewHolder.dateText = (TextView)view.findViewById(R.id.tv_date);
		viewHolder.unreadCountText = (TextView)view.findViewById(R.id.tv_unread_count);
		viewHolder.avatar = (ImageView)view.findViewById(R.id.avatar);
		view.setTag(viewHolder);
		
		return view;
	}
	
	static class ViewHolder {
		TextView nameText;
		TextView messageText;
		TextView dateText;
		TextView unreadCountText;
		ImageView avatar;
	}
}