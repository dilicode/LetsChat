package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;

public class ChatMessageTableHelper {
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ChatMessageTable.TABLE_NAME + " (" +
		    ChatMessageTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ChatMessageTable.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_MESSAGE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TYPE + " INTEGER" + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_STATUS + " INTEGER" + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TIME + " LONG" + 
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ChatMessageTable.TABLE_NAME;
	
	public static final int TYPE_INCOMING = 1;
	public static final int TYPE_OUTGOING = 2;
	
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_PENDING = 2;
	public static final int STATUS_FAILURE = 3;
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static ContentValues newIncomingMessageContentValues(String jid, String body, long timeMillis) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, TYPE_INCOMING);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_SUCCESS);
		
		return values;
	}
	
	public static ContentValues newOutgoingMessageContentValues(String jid, String body, long timeMillis) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, TYPE_OUTGOING);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_PENDING);
		
		return values;
	}
	
	public static ContentValues newSuccessStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_SUCCESS);
		
		return values;
	}
	
	public static ContentValues newFailureStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_SUCCESS);
		
		return values;
	}
	
	public static boolean isIncomingMessage(int type) {
		return type == TYPE_INCOMING;
	}
}