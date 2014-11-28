package com.mstr.letschat.databases;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.ContactRequest;

public class ContactRequestTableHelper {
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + ContactRequestTable.TABLE_NAME + " (" +
			ContactRequestTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			ContactRequestTable.COLUMN_NAME_ORIGIN + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
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
	
	public boolean insert(ContactRequest request) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_ORIGIN, request.getOrigin());
		values.put(ContactRequestTable.COLUMN_NAME_NICKNAME, request.getNickname());
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, request.getStatus());
		
		return db.insert(ContactRequestTable.TABLE_NAME, null, values) != -1;
	}
	
	public boolean insertIfNonExisting(ContactRequest request) {
		if (!checkExistence(request.getOrigin())) {
			return insert(request);
		} else {
			return false;
		}
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
			return ContentUris.withAppendedId(ContactRequestTable.CONTENT_ID_URI_BASE, rowId);
		}
		
		throw new SQLException("Failed to insert row into " + ContactRequestTable.TABLE_NAME);
	}
	
	public int update(ContentValues values, String whereClause, String[] whereArgs) {
		return dbHelper.getWritableDatabase().update(ContactRequestTable.TABLE_NAME, values, whereClause, whereArgs);
	}
	
	public List<ContactRequest> query() {
		List<ContactRequest> result = new ArrayList<ContactRequest>();
		
		String[] projection = {
				ContactRequestTable.COLUMN_NAME_ORIGIN,
				ContactRequestTable.COLUMN_NAME_NICKNAME,
				ContactRequestTable.COLUMN_NAME_STATUS};
		
		Cursor cursor = dbHelper.getReadableDatabase().query(ContactRequestTable.TABLE_NAME, 
				projection, null, null, null, null,
				ContactRequestTable._ID + " DESC");
		if (cursor.moveToFirst()) {
			do {
				String origin = cursor.getString(cursor.getColumnIndexOrThrow(ContactRequestTable.COLUMN_NAME_ORIGIN));
				String nickname = cursor.getString(cursor.getColumnIndexOrThrow(ContactRequestTable.COLUMN_NAME_NICKNAME));
				int status = cursor.getInt(cursor.getColumnIndexOrThrow(ContactRequestTable.COLUMN_NAME_STATUS));
				
				result.add(new ContactRequest(origin, nickname, status));
			} while (cursor.moveToNext());
		}
		
		return result;
	}
	
	public boolean checkExistence(String origin) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {ContactRequestTable._ID};
		
		String selection = ContactRequestTable.COLUMN_NAME_ORIGIN + " = ?";
		String[] selectionArgs = {origin};
		
		Cursor cursor = db.query(ContactRequestTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
		
		return cursor.moveToFirst();
	}
	
	public void updateAsAccepted(String origin) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, ContactRequest.STATUS_ACCPTED);
		
		String selection = ContactRequestTable.COLUMN_NAME_ORIGIN + " = ?";
		String[] selectionArgs = {origin};
		
		db.update(ContactRequestTable.TABLE_NAME, values, selection, selectionArgs);
	}
}