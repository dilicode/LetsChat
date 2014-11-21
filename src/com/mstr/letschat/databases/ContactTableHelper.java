package com.mstr.letschat.databases;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.ContactTableEntry;
import com.mstr.letschat.model.Contact;

public class ContactTableHelper {
	private static final String SQL_CREATE_ENTRIES =
		    "CREATE TABLE " + ContactTableEntry.TABLE_NAME + " (" +
		    ContactTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		    ContactTableEntry.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
		    ContactTableEntry.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ContactTableEntry.TABLE_NAME;
	
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
	
	
	public boolean insert(Contact contact) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactTableEntry.COLUMN_NAME_NICKNAME, contact.getNickname());
		values.put(ContactTableEntry.COLUMN_NAME_JID, contact.getJid());
		
		return db.insert(ContactTableEntry.TABLE_NAME, null, values) != -1;
	}
	
	public List<Contact> query() {
		List<Contact> result = new ArrayList<Contact>();
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {
				ContactTableEntry.COLUMN_NAME_NICKNAME,
				ContactTableEntry.COLUMN_NAME_JID};
		
		String orderBy = ContactTableEntry.COLUMN_NAME_NICKNAME + " COLLATE LOCALIZED ASC";
		
		Cursor cursor = db.query(ContactTableEntry.TABLE_NAME, projection, null, null, null, null, orderBy);
		if (cursor.moveToFirst()) {
			do {
				String jid = cursor.getString(cursor.getColumnIndexOrThrow(ContactTableEntry.COLUMN_NAME_JID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactTableEntry.COLUMN_NAME_NICKNAME));
				
				result.add(new Contact(jid, name));
			} while (cursor.moveToNext());
		}
		
		return result;
	}
	
	public Contact queryByJid(String jid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {ContactTableEntry.COLUMN_NAME_NICKNAME};
		
		String selection = ContactTableEntry.COLUMN_NAME_JID + " = ?";
		String[] selectionArgs = {jid};
		
		Cursor cursor = db.query(ContactTableEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		if (cursor.moveToFirst()) {
			String nickname = cursor.getString(cursor.getColumnIndexOrThrow(ContactTableEntry.COLUMN_NAME_NICKNAME));
			
			return new Contact(jid, nickname);
		}
		
		return null;
	}
}