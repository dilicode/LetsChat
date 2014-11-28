package com.mstr.letschat.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;

public class CustomProvider extends ContentProvider {
	public static final String AUTHORITY = "com.mstr.letschat.provider";
	
	public static final int CONTACT = 1;
	public static final int CONTACT_ID = 2;
	
	public static final int CONTACT_REQUEST = 3;
	public static final int CONTACT_REQUEST_ID = 4;
	
	private final UriMatcher uriMatcher;
	
	private ContactTableHelper contactTableHelper;
	private ContactRequestTableHelper contactRequestTableHelper;
	
	public CustomProvider() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		uriMatcher.addURI(AUTHORITY, ContactTable.TABLE_NAME, CONTACT);
		uriMatcher.addURI(AUTHORITY, ContactTable.TABLE_NAME + "/#", CONTACT_ID);
		
		uriMatcher.addURI(AUTHORITY, ContactRequestTable.TABLE_NAME, CONTACT_REQUEST);
		uriMatcher.addURI(AUTHORITY, ContactRequestTable.TABLE_NAME + "/#", CONTACT_REQUEST_ID);
	}
	
	@Override
	public boolean onCreate() {
		contactTableHelper = ContactTableHelper.getInstance(getContext());
		contactRequestTableHelper = ContactRequestTableHelper.getInstance(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor result = null;
		
		switch (uriMatcher.match(uri)) {
		case CONTACT:
			result = contactTableHelper.query(projection, selection, selectionArgs, sortOrder);
			break;
			
		case CONTACT_ID:
			selection = ContactTable._ID + "=?";
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			result = contactTableHelper.query(projection, selection, selectionArgs, sortOrder);
			break;
			
		case CONTACT_REQUEST:
			result = contactRequestTableHelper.query(projection, selection, selectionArgs, sortOrder);
			break;
			
		case CONTACT_REQUEST_ID:
			selection = ContactRequestTable._ID + "=?";
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			result = contactRequestTableHelper.query(projection, selection, selectionArgs, sortOrder);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		result.setNotificationUri(getContext().getContentResolver(), uri);
		
		return result;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CONTACT:
			return ContactTable.CONTENT_TYPE;
		
		case CONTACT_ID:
			return ContactTable.CONTENT_ITEM_TYPE;
			
		case CONTACT_REQUEST:
			return ContactRequestTable.CONTENT_TYPE;
			
		case CONTACT_REQUEST_ID:
			return ContactRequestTable.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri result = null;
		switch (uriMatcher.match(uri)) {
		case CONTACT:
			result = contactTableHelper.insert(values);
			break;
			
		case CONTACT_REQUEST:
			result = contactRequestTableHelper.insert(values);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(result, null);
		
		return result;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		switch (uriMatcher.match(uri)) {
		case CONTACT_ID:
			String finalWhere = DatabaseUtils.concatenateWhere(ContactTable._ID + " = " + ContentUris.parseId(uri), where);
			int count = contactTableHelper.delete(finalWhere, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			
			return count;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case CONTACT_REQUEST_ID:
			String finalWhere = DatabaseUtils.concatenateWhere(ContactRequestTable._ID + " = " + ContentUris.parseId(uri), where);
			count = contactRequestTableHelper.update(values, finalWhere, whereArgs);
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}
}