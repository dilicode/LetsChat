package com.mstr.letschat.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactTableEntry;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.utils.PreferenceUtils;

public class ContactTableHelper {
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ContactTableEntry.TABLE_NAME + " (" +
		    ContactTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ContactTableEntry.COLUMN_NAME_CONTACT_ID + TEXT_TYPE + COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_USER + TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactTableEntry.TABLE_NAME;
	
	private ChatDbHelper dbHelper;
	private Context context;
	
	public ContactTableHelper(Context context) {
		dbHelper = new ChatDbHelper(context);
		this.context = context;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean insert(Contact contact) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactTableEntry.COLUMN_NAME_CONTACT_NAME, contact.getName());
		values.put(ContactTableEntry.COLUMN_NAME_CONTACT_ID, contact.getUser());
		values.put(ContactTableEntry.COLUMN_NAME_USER, PreferenceUtils.getUser(context));
		
		return db.insert(ContactTableEntry.TABLE_NAME, null, values) != -1;
	}
}