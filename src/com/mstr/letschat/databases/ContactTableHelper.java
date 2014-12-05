package com.mstr.letschat.databases;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

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
	
	private ChatDbHelper dbHelper;
	
	private static ContactTableHelper instance;
	
	public static synchronized ContactTableHelper getInstance(Context context) {
		if (instance == null) {
			instance = new ContactTableHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	private ContactTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
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
			orderBy = ContactTable.DEFAULT_SORT_ORDER;
		}
		
		return db.query(ContactTable.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
	}
	
	public Uri insert(ContentValues values) {
		long rowId = dbHelper.getWritableDatabase().insert(ContactTable.TABLE_NAME, null, values);
		if (rowId > 0) {
			return ContentUris.withAppendedId(ContactTable.CONTENT_URI, rowId);
		}
		
		throw new SQLException("Failed to insert row into " + ContactTable.TABLE_NAME);
	}
	
	public boolean isContact(String jid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {ContactTable._ID};
		String selection = ContactTable.COLUMN_NAME_JID + " = ?";
		String[] selectionArgs = {jid};
		
		Cursor cursor = db.query(ContactTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		return cursor.moveToFirst();
	}
	
	public int delete(String whereClause, String[] whereArgs) {
		return dbHelper.getWritableDatabase().delete(ContactTable.TABLE_NAME, whereClause, whereArgs);
	}
}