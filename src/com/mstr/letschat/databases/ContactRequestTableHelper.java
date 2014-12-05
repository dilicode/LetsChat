package com.mstr.letschat.databases;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

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
	
	private ChatDbHelper dbHelper;
	
	private static ContactRequestTableHelper instance;
	
	private ContactRequestTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
	}
	
	public static synchronized ContactRequestTableHelper getInstance(Context context) {
		if (instance == null) {
			instance = new ContactRequestTableHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public Cursor query(String[] projection, String selection,
			String[] selectionArgs, String orderBy) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		if (orderBy == null) {
			orderBy = ContactRequestTable.DEFAULT_SORT_ORDER;
		}
		
		return db.query(ContactRequestTable.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
	}
	
	public Uri insert(ContentValues values) {
		long rowId = dbHelper.getWritableDatabase().insert(ContactRequestTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			return ContentUris.withAppendedId(ContactRequestTable.CONTENT_URI, rowId);
		}
		
		throw new SQLException("Failed to insert row into " + ContactRequestTable.TABLE_NAME);
	}
	
	public int update(ContentValues values, String whereClause, String[] whereArgs) {
		return dbHelper.getWritableDatabase().update(ContactRequestTable.TABLE_NAME, values, whereClause, whereArgs);
	}
}