package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.UserLocation;

public class ChatMessageTableHelper {
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ChatMessageTable.TABLE_NAME + " (" +
		    ChatMessageTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ChatMessageTable.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_MESSAGE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TYPE + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_STATUS + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TIME + " LONG" + ChatDbHelper.COMMA_SEP +
			ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
			ChatMessageTable.COLUMN_NAME_LATITUDE + " DOUBLE" + ChatDbHelper.COMMA_SEP +
			ChatMessageTable.COLUMN_NAME_LONGITUDE + " DOUBLE" + ChatDbHelper.COMMA_SEP +
			ChatMessageTable.COLUMN_NAME_ADDRESS + ChatDbHelper.TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ChatMessageTable.TABLE_NAME;
	
	public static final int TYPE_INCOMING = 1;
	public static final int TYPE_OUTGOING = 2;
	
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_PENDING = 2;
	public static final int STATUS_FAILURE = 3;

	public static final int MESSAGE_TYPE_PLAIN_TEXT = 1;
	public static final int MESSAGE_TYPE_LOCATION = 2;
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static ContentValues newPlainTextMessage(String jid, String body, long timeMillis, boolean outgoing) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE, MESSAGE_TYPE_PLAIN_TEXT);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING : TYPE_INCOMING);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		
		return values;
	}

	public static ContentValues newLocationMessage(String jid, String body, long timeMillis, UserLocation location, boolean outgoing) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE, MESSAGE_TYPE_LOCATION);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING : TYPE_INCOMING);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_LATITUDE, location.getLatitude());
		values.put(ChatMessageTable.COLUMN_NAME_LONGITUDE, location.getLongitude());
		values.put(ChatMessageTable.COLUMN_NAME_ADDRESS, location.getAddress());

		return values;
	}
	
	public static ContentValues newSuccessStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_SUCCESS);
		
		return values;
	}
	
	public static ContentValues newFailureStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_FAILURE);
		
		return values;
	}


	public static boolean isIncomingMessage(Cursor cursor) {
		// type is used to identify incoming or outgoing
		return cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE)) == TYPE_INCOMING;
	}

	public static boolean isPlainTextMessage(Cursor cursor) {
		// message type is used to identify whether it is a plain text message, or location message, etc.
		return cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE)) == MESSAGE_TYPE_PLAIN_TEXT;
	}

	public static boolean isLocationMessage(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE)) == MESSAGE_TYPE_LOCATION;
	}

	public static void updateToVersion2(SQLiteDatabase db) {
		final String sqlAddMessageType = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE + ChatDbHelper.INTEGER_TYPE;
		final String sqlAddLatitude = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_LATITUDE + " DOUBLE";
		final String sqlAddLongitude = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_LONGITUDE + " DOUBLE";
		final String sqlAddAddress = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_ADDRESS + ChatDbHelper.TEXT_TYPE;

		db.execSQL(sqlAddMessageType);
		db.execSQL(sqlAddLatitude);
		db.execSQL(sqlAddLongitude);
		db.execSQL(sqlAddAddress);

		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE_TYPE, 1);
		int count = db.update(ChatMessageTable.TABLE_NAME, values, null, null);
		AppLog.d(count + " rows updated to plain text message");
	}
}