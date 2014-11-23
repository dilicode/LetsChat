package com.mstr.letschat.databases;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mstr.letschat.databases.ChatContract.IncomingContactRequestTableEntry;
import com.mstr.letschat.model.ContactRequest;

public class IncomingRequestTableHelper {
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + IncomingContactRequestTableEntry.TABLE_NAME + " (" +
			IncomingContactRequestTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			IncomingContactRequestTableEntry.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
			IncomingContactRequestTableEntry.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
			IncomingContactRequestTableEntry.COLUMN_NAME_STATUS + ChatDbHelper.INTEGER_TYPE +
		    " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + IncomingContactRequestTableEntry.TABLE_NAME;
	
	private ChatDbHelper dbHelper;
	
	private static IncomingRequestTableHelper instance;
	
	private IncomingRequestTableHelper(Context context) {
		dbHelper = ChatDbHelper.getInstance(context);
	}
	
	public static synchronized IncomingRequestTableHelper getInstance(Context context) {
		if (instance == null) {
			instance = new IncomingRequestTableHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);
	}
	
	public boolean insert(ContactRequest request) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(IncomingContactRequestTableEntry.COLUMN_NAME_JID, request.getJid());
		values.put(IncomingContactRequestTableEntry.COLUMN_NAME_NICKNAME, request.getNickname());
		values.put(IncomingContactRequestTableEntry.COLUMN_NAME_STATUS, request.getStatus());
		
		return db.insert(IncomingContactRequestTableEntry.TABLE_NAME, null, values) != -1;
	}
	
	public List<ContactRequest> query() {
		List<ContactRequest> result = new ArrayList<ContactRequest>();
		
		String[] projection = {
				IncomingContactRequestTableEntry.COLUMN_NAME_JID,
				IncomingContactRequestTableEntry.COLUMN_NAME_NICKNAME,
				IncomingContactRequestTableEntry.COLUMN_NAME_STATUS};
		
		Cursor cursor = dbHelper.getReadableDatabase().query(IncomingContactRequestTableEntry.TABLE_NAME, 
				projection, null, null, null, null,
				IncomingContactRequestTableEntry._ID + " DESC");
		if (cursor.moveToFirst()) {
			do {
				String from = cursor.getString(cursor.getColumnIndexOrThrow(IncomingContactRequestTableEntry.COLUMN_NAME_JID));
				String nickname = cursor.getString(cursor.getColumnIndexOrThrow(IncomingContactRequestTableEntry.COLUMN_NAME_NICKNAME));
				int status = cursor.getInt(cursor.getColumnIndexOrThrow(IncomingContactRequestTableEntry.COLUMN_NAME_STATUS));
				
				result.add(new ContactRequest(from, nickname, status));
			} while (cursor.moveToNext());
		}
		
		return result;
	}
}