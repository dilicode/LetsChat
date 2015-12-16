package com.mstr.letschat.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.views.IncomingLocationView;
import com.mstr.letschat.views.IncomingPlainTextView;
import com.mstr.letschat.views.LocationView;
import com.mstr.letschat.views.MessageView;
import com.mstr.letschat.views.OutgoingLocationView;
import com.mstr.letschat.views.OutgoingPlainTextView;
import com.mstr.letschat.views.PlainTextView;
import com.mstr.letschat.xmpp.UserLocation;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageCursorAdapter extends CursorAdapter {
	private static final int VIEW_TYPE_COUNT = 4;
	private static final int VIEW_TYPE_INCOMING_PLAIN_TEXT = 0;
	private static final int VIEW_TYPE_OUTGOING_PLAIN_TEXT = 1;
	private static final int VIEW_TYPE_INCOMING_LOCATION = 2;
	private static final int VIEW_TYPE_OUTGOING_LOCATION = 3;

	private DateFormat timeFormat;
	private DateFormat dateFormat;
	
	public MessageCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		
		timeFormat = new SimpleDateFormat("HH:mm");
		dateFormat = DateFormat.getDateInstance();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		MessageView messageView = (MessageView)view;

		if (ChatMessageTableHelper.isPlainTextMessage(cursor)) {
			bindPlainTextMessage((PlainTextView)messageView, cursor);
		} else if (ChatMessageTableHelper.isLocationMessage(cursor)) {
			bindLocation((LocationView)view, cursor);
		}

		// set message status, sent or pending, for example
		bindStatus(messageView, cursor);

		// whether to display date at header
		bindDateSeparator(messageView, cursor);
	}

	private void bindPlainTextMessage(PlainTextView view, Cursor cursor) {
		view.setMessageText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)));
	}

	private void bindStatus(MessageView view, Cursor cursor) {
		if (!ChatMessageTableHelper.isIncomingMessage(cursor)) {
			int status = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_STATUS));
			view.showProgress(status == ChatMessageTableHelper.STATUS_SUCCESS);
		}
	}

	private void bindDateSeparator(MessageView view, Cursor cursor) {
		long timeMillis = getTime(cursor);

		view.setTimeText(timeFormat.format(new Date(timeMillis)));

		if (isSameDayToPreviousPosition(timeMillis, cursor)) {
			view.hideDateSeparator();
		} else {
			view.displayDateSeparator(dateFormat.format(new Date(timeMillis)));
		}
	}

	private void bindLocation(LocationView view, Cursor cursor) {
		UserLocation location = new UserLocation(cursor);
		// Get the UserLocation for this item and attach it to the MapView
		view.getMapView().setTag(location);

		// Ensure the map has been initialised by the on map ready callback in ViewHolder.
		// If it is not ready yet, it will be initialised with the NamedLocation set as its tag
		// when the callback is received.
		if (view.getMap() != null) {
			// The map is already ready to be used
			view.setMapLocation(location);
		}

		view.setAddress(location.getAddress());
		view.setName(location.getName());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int viewType = getItemViewType(cursor);
		View view = null;
		switch (viewType) {
			case VIEW_TYPE_INCOMING_PLAIN_TEXT:
				view = new IncomingPlainTextView(context);
				break;

			case VIEW_TYPE_OUTGOING_PLAIN_TEXT:
				view = new OutgoingPlainTextView(context);
				break;

			case VIEW_TYPE_INCOMING_LOCATION:
				view = new IncomingLocationView(context);
				((LocationView)view).initializeMapView();
				break;

			case VIEW_TYPE_OUTGOING_LOCATION:
				view = new OutgoingLocationView(context);
				((LocationView)view).initializeMapView();
				break;
		}
		if (view != null) {
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

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
		if (ChatMessageTableHelper.isIncomingMessage(cursor)) {
			if (ChatMessageTableHelper.isPlainTextMessage(cursor)) {
				return VIEW_TYPE_INCOMING_PLAIN_TEXT;
			} else {
				return VIEW_TYPE_INCOMING_LOCATION;
			}
		} else {
			if (ChatMessageTableHelper.isPlainTextMessage(cursor)) {
				return VIEW_TYPE_OUTGOING_PLAIN_TEXT;
			} else {
				return VIEW_TYPE_OUTGOING_LOCATION;
			}
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
}