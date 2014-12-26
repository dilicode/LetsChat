package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatContract.ConversationTable;

public class ConversationTableHelper {
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ConversationTable.TABLE_NAME + " (" +
		    ConversationTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ConversationTable.COLUMN_NAME_NAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ConversationTable.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ConversationTable.COLUMN_NAME_LATEST_MESSAGE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ConversationTable.COLUMN_NAME_UNREAD + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
		    ConversationTable.COLUMN_NAME_TIME + " LONG" +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ConversationTable.TABLE_NAME;
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static ContentValues newUpdateContentValues(String message, long timeMillis) {
		ContentValues values = new ContentValues();
		values.put(ConversationTable.COLUMN_NAME_LATEST_MESSAGE, message);
		values.put(ConversationTable.COLUMN_NAME_TIME, timeMillis);
		
		return values;
	}
	
	public static ContentValues newInsertContentValues(String name, String nickname, String message, long timeMillis, int unread) {
		ContentValues values = new ContentValues();
		values.put(ConversationTable.COLUMN_NAME_NAME, name);
		values.put(ConversationTable.COLUMN_NAME_NICKNAME, nickname);
		values.put(ConversationTable.COLUMN_NAME_LATEST_MESSAGE, message);
		values.put(ConversationTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ConversationTable.COLUMN_NAME_UNREAD, unread);
		
		return values;
	}
}