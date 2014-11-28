package com.mstr.letschat.databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.UserSearchResult;

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
			return ContentUris.withAppendedId(ContactTable.CONTENT_ID_URI_BASE, rowId);
		}
		
		throw new SQLException("Failed to insert row into " + ContactTable.TABLE_NAME);
	}
	
	
	public boolean insert(Contact contact) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactTable.COLUMN_NAME_NICKNAME, contact.getNickname());
		values.put(ContactTable.COLUMN_NAME_JID, contact.getJid());
		
		return db.insert(ContactTable.TABLE_NAME, null, values) != -1;
	}
	
	public List<Contact> query() {
		List<Contact> result = new ArrayList<Contact>();
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {
				ContactTable.COLUMN_NAME_NICKNAME,
				ContactTable.COLUMN_NAME_JID};
		
		String orderBy = ContactTable.COLUMN_NAME_NICKNAME + " COLLATE LOCALIZED ASC";
		
		Cursor cursor = db.query(ContactTable.TABLE_NAME, projection, null, null, null, null, orderBy);
		if (cursor.moveToFirst()) {
			do {
				String jid = cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_NAME_JID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_NAME_NICKNAME));
				
				result.add(new Contact(jid, name));
			} while (cursor.moveToNext());
		}
		
		return result;
	}
	
	public Contact queryByJid(String jid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {ContactTable.COLUMN_NAME_NICKNAME};
		
		String selection = ContactTable.COLUMN_NAME_JID + " = ?";
		String[] selectionArgs = {jid};
		
		Cursor cursor = db.query(ContactTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		if (cursor.moveToFirst()) {
			String nickname = cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_NAME_NICKNAME));
			
			return new Contact(jid, nickname);
		}
		
		return null;
	}
	
	public void torename(ArrayList<UserSearchResult> users) {
		Map<String, UserSearchResult> map = new HashMap<String, UserSearchResult>();
		for (UserSearchResult user : users) {
			map.put(user.getJid(), user);
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		List<String> jidsInDb = new ArrayList<String>();
		String[] jids = new String[users.size()];
		
		String sql = "SELECT " + ContactTable.COLUMN_NAME_JID + " FROM " + ContactTable.TABLE_NAME +
				" WHERE " + ContactTable.COLUMN_NAME_JID + " IN (" + makePlaceholders(jids.length) + ")";
		Cursor cursor = db.rawQuery(sql, map.keySet().toArray(jids));
		if (cursor.moveToFirst()) {
			do {
				jidsInDb.add(cursor.getString(cursor.getColumnIndexOrThrow(ContactTable.COLUMN_NAME_JID)));
			} while (cursor.moveToNext());
		}
		
		for (String jid : jidsInDb) {
			map.get(jid).setStatus(UserSearchResult.STATUS_CONTACT);
		}
	}
	
	public int delete(String whereClause, String[] whereArgs) {
		return dbHelper.getWritableDatabase().delete(ContactTable.TABLE_NAME, whereClause, whereArgs);
	}
	
	private String makePlaceholders(int size) {
		if (size < 1) {
			// It will lead to an invalid query anyway ..
			throw new RuntimeException("No placeholders");
		} else { 
			StringBuilder sb = new StringBuilder();
			sb.append("?");
			for (int i = 1; i < size; i++) {
				sb.append(",?");
			}
			
			return sb.toString();
	    }
	}
}