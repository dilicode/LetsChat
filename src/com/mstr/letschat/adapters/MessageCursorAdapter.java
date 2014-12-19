package com.mstr.letschat.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;

public class MessageCursorAdapter extends CursorAdapter {
	private static final int VIEW_TYPE_COUNT = 3;
	private static final int VIEW_TYPE_INCOMING_MESSAGE = 0;
	private static final int VIEW_TYPE_OUTGOING_MESSAGE = 1;
	private static final int VIEW_TYPE_DATE = 2;
	
	private DateFormat dateFormat;
	
	private LayoutInflater inflater;
	
	public MessageCursorAdapter(Context context, Cursor c, int flags) {
		super(context,  c, flags);
		
		dateFormat = new SimpleDateFormat("HH:mm");
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
	
		
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		
		int position = cursor.getPosition();
		int lastPosition = viewHolder.position;
		
		if (isMessageView(viewHolder.type)) {
			viewHolder.messageText.setText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)));
			viewHolder.timeText.setText(getDisplayTime(cursor));
		} else {
			
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int viewType = getItemViewType(cursor);
		
		View view = inflater.inflate(getLayout(viewType), parent, false);
		
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.messageText = (TextView)view.findViewById(R.id.tv_message);
		viewHolder.timeText = (TextView)view.findViewById(R.id.tv_time);
		viewHolder.type = viewType;
		viewHolder.position = cursor.getPosition();
		view.setTag(viewHolder);
		
		return view;
	 }
	
	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor)getItem(position);
		
		return getItemViewType(cursor);
	}
	
	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}
    
	private int getItemViewType(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		if (ChatMessageTableHelper.isIncomingMessage(type)) {
			return VIEW_TYPE_INCOMING_MESSAGE;
		} else {
			return VIEW_TYPE_OUTGOING_MESSAGE;
		}
	}
	
	private int getLayout(int viewType) {
		if (viewType == VIEW_TYPE_INCOMING_MESSAGE) {
			return R.layout.incoming_message_list_item;
		}
		
		if (viewType == VIEW_TYPE_OUTGOING_MESSAGE) {
			return R.layout.outgoing_message_list_item;
		}
		
		return 0;
	}
	
	private boolean isMessageView(int viewType) {
		return viewType != VIEW_TYPE_DATE;
	}
	
	private String getDisplayTime(Cursor cursor) {
		long time = cursor.getLong(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TIME));
		return dateFormat.format(new Date(time));
	}
	
	static class ViewHolder {
		TextView messageText;
		TextView timeText;
		int type;
		int position;
	}
}