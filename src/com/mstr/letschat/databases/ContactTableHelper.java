package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactTableEntry;

public class ContactTableHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ContactTableEntry.TABLE_NAME + " (" +
		    ContactTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ContactTableEntry.COLUMN_NAME_CONTACT_USER + TEXT_TYPE + COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_CONTACT_NAME + TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactTableEntry.TABLE_NAME;
	
	private ChatDbHelper dbHelper;
	
	public ContactTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public boolean insert(String user, String name) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactTableEntry.COLUMN_NAME_CONTACT_NAME, name);
		values.put(ContactTableEntry.COLUMN_NAME_CONTACT_USER, user);
		
		return db.insert(ContactTableEntry.TABLE_NAME, null, values) != -1;
	}
}