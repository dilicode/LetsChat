package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.SentContactRequestTableEntry;

public class SentContactRequestTableHelper {
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + SentContactRequestTableEntry.TABLE_NAME + " (" +
			SentContactRequestTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			SentContactRequestTableEntry.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + SentContactRequestTableEntry.TABLE_NAME;
	
	private ChatDbHelper dbHelper;
	
	private static SentContactRequestTableHelper instance;
	
	private SentContactRequestTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
	}
	
	public static synchronized SentContactRequestTableHelper getInstance(Context context) {
		if (instance == null) {
			instance = new SentContactRequestTableHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public boolean insertIfNonExisting(String jid) {
		if (!checkExistence(jid)) {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(SentContactRequestTableEntry.COLUMN_NAME_JID, jid);
			
			return db.insert(SentContactRequestTableEntry.TABLE_NAME, null, values) != -1;
		} else {
			return false;
		}
	}
	
	public Boolean checkExistence(String jid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {SentContactRequestTableEntry._ID};
		
		String selection = SentContactRequestTableEntry.COLUMN_NAME_JID + " = ?";
		String[] selectionArgs = {jid};
		
		Cursor cursor = db.query(SentContactRequestTableEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		
		return cursor.moveToFirst();
	}
}
