package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactTable;

public class ContactTableHelper {
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ContactTable.TABLE_NAME + " (" +
		    ContactTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ContactTable.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ContactTable.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactTable.TABLE_NAME;
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static ContentValues newContentValues(String jid, String nickname) {
		ContentValues values = new ContentValues();
		values.put(ContactTable.COLUMN_NAME_JID, jid);
		values.put(ContactTable.COLUMN_NAME_NICKNAME, nickname);
		
		return values;
	}
}