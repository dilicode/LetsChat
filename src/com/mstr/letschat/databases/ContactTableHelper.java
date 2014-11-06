package com.mstr.letschat.databases;

import org.jivesoftware.smack.RosterEntry;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactTableEntry;

public class ContactTableHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ContactTableEntry.TABLE_NAME + " (" +
		    ContactTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ContactTableEntry.COLUMN_NAME_USER + TEXT_TYPE + COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_OWNER + TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactTableEntry.TABLE_NAME;
	
	private static ChatDbHelper dbHelper;
	
	public static void init(ChatDbHelper helper) {
		dbHelper = helper;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public static boolean insert(String user, RosterEntry entry) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ContactTableEntry.COLUMN_NAME_NAME, entry.getName());
		values.put(ContactTableEntry.COLUMN_NAME_USER, entry.getUser());
		values.put(ContactTableEntry.COLUMN_NAME_OWNER, user);

		// Insert the new row, returning the primary key value of the new row
		return db.insert(ContactTableEntry.TABLE_NAME, null, values) != -1;
	}
}
