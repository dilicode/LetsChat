package com.mstr.letschat.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;

public class MessageCursorAdapter extends CursorAdapter {
	private static final int VIEW_TYPE_COUNT = 2;
	private static final int VIEW_TYPE_INCOMING_MESSAGE = 0;
	private static final int VIEW_TYPE_OUTGOING_MESSAGE = 1;
	
	private DateFormat dateFormat;
	
	private LayoutInflater inflater;
	
	public MessageCursorAdapter(Context context, Cursor c, int flags) {
		super(context,  c, flags);
		
		dateFormat = new SimpleDateFormat("HH:mm");
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder)view.getTag();
		
		viewHolder.messageText.setText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)));
		
		long timeMillis = getTime(cursor);
		viewHolder.timeText.setText(dateFormat.format(new Date(timeMillis)));
		
		if (viewHolder.type == VIEW_TYPE_OUTGOING_MESSAGE) {
			int status = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_STATUS));
			if (status == ChatMessageTableHelper.STATUS_SUCCESS) {
				((OutMessageViewHolder)viewHolder).progressBar.setVisibility(View.GONE);
			} else {
				((OutMessageViewHolder)viewHolder).progressBar.setVisibility(View.VISIBLE);
			}
		}
		
		if (isSameDayToPreviousPosition(timeMillis, cursor)) {
			viewHolder.dateText.setVisibility(View.GONE);
		} else {
			viewHolder.dateText.setText(DateFormat.getDateInstance().format(new Date(timeMillis)));
			viewHolder.dateText.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int viewType = getItemViewType(cursor);
		
		View view = inflater.inflate(getLayout(viewType), parent, false);
		
		ViewHolder viewHolder = null;
		if (viewType == VIEW_TYPE_INCOMING_MESSAGE) {
			viewHolder = new ViewHolder();
		} else {
			viewHolder = new OutMessageViewHolder();
			((OutMessageViewHolder)viewHolder).progressBar = (ProgressBar)view.findViewById(R.id.message_sending_progress);
		}
		viewHolder.messageText = (TextView)view.findViewById(R.id.tv_message);
		viewHolder.timeText = (TextView)view.findViewById(R.id.tv_time);
		viewHolder.dateText = (TextView)view.findViewById(R.id.tv_date);
		viewHolder.type = viewType;
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
		} else {
			return R.layout.outgoing_message_list_item;
		}
	}
	
	private boolean isSameDayToPreviousPosition(long time, Cursor cursor) {
		// get previous item's date, for comparison
		if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
			long prevTime = getTime(cursor);
	        cursor.moveToNext();
	        
	        Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTimeInMillis(time);
			cal2.setTimeInMillis(prevTime);
			return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
			                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	    } else {
	    	return false;
	    }
	}
	
	private long getTime(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TIME));
	}
	
	static class ViewHolder {
		TextView messageText;
		TextView timeText;
		TextView dateText;
		int type;
	}
	
	static class OutMessageViewHolder extends ViewHolder {
		ProgressBar progressBar;
	}
}