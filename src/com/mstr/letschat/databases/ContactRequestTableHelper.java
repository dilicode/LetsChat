package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;

public class ContactRequestTableHelper {
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + ContactRequestTable.TABLE_NAME + " (" +
			ContactRequestTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			ContactRequestTable.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
			ContactRequestTable.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
			ContactRequestTable.COLUMN_NAME_STATUS + ChatDbHelper.INTEGER_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactRequestTable.TABLE_NAME;
	
	public static final int CONTACT_REQUEST_STATUS_PENDING = 1;
	public static final int CONTACT_REQUEST_STATUS_ACCPTED = 2;
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static ContentValues newContentValues(String jid, String nickname) {
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_JID, jid);
		values.put(ContactRequestTable.COLUMN_NAME_NICKNAME, nickname);
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, CONTACT_REQUEST_STATUS_PENDING);
		
		return values;
	}
	
	public static ContentValues newContentValuesWithAcceptedStatus() {
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, CONTACT_REQUEST_STATUS_ACCPTED);
		
		return values;
	}
	
	public static boolean isAcceptedStatus(int status) {
		return status == ContactRequestTableHelper.CONTACT_REQUEST_STATUS_ACCPTED;
	}
}