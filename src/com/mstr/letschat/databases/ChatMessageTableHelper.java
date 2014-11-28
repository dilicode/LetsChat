package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.model.ChatMessage;

public class ChatMessageTableHelper {
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ChatMessageTable.TABLE_NAME + " (" +
		    ChatMessageTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ChatMessageTable.COLUMN_NAME_SENDER + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_MESSAGE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TYPE + " INTEGER" + ChatDbHelper.COMMA_SEP +
		    ChatMessageTable.COLUMN_NAME_TIME + " LONG" + 
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ChatMessageTable.TABLE_NAME;
	
	private ChatDbHelper dbHelper;
	
	private static ChatMessageTableHelper instance;
	
	private ChatMessageTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
	}
	
	public static synchronized ChatMessageTableHelper getInstance(Context context) {
		if (instance == null) {
			instance = new ChatMessageTableHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public boolean insert(ChatMessage message) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_SENDER, message.getJid());
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, message.getBody());
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, message.getType());
		values.put(ChatMessageTable.COLUMN_NAME_TIME, message.getTime());
		
		return dbHelper.getWritableDatabase().insert(ChatMessageTable.TABLE_NAME, null, values) != -1;
	}
}
