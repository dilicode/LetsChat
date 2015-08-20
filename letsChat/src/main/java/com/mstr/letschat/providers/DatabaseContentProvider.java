package com.mstr.letschat.providers;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ChatContract.ConversationTable;
import com.mstr.letschat.databases.ChatDbHelper;

import java.util.ArrayList;

public class DatabaseContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.mstr.letschat.provider";
	
	private static final int CONTACT = 1;
	private static final int CONTACT_ID = 2;
	
	private static final int CONTACT_REQUEST = 3;
	private static final int CONTACT_REQUEST_ID = 4;
	
	private static final int CHAT_MESSAGE = 5;
	private static final int CHAT_MESSAGE_ID = 6;
	
	private static final int CONVERSATION = 7;
	private static final int CONVERSATION_ID = 8;
	
	private final UriMatcher uriMatcher;
	
	private ChatDbHelper dbHelper;
	
	public DatabaseContentProvider() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		uriMatcher.addURI(AUTHORITY, ContactTable.TABLE_NAME, CONTACT);
		uriMatcher.addURI(AUTHORITY, ContactTable.TABLE_NAME + "/#", CONTACT_ID);
		
		uriMatcher.addURI(AUTHORITY, ContactRequestTable.TABLE_NAME, CONTACT_REQUEST);
		uriMatcher.addURI(AUTHORITY, ContactRequestTable.TABLE_NAME + "/#", CONTACT_REQUEST_ID);
		
		uriMatcher.addURI(AUTHORITY, ChatMessageTable.TABLE_NAME, CHAT_MESSAGE);
		uriMatcher.addURI(AUTHORITY, ChatMessageTable.TABLE_NAME + "/#", CHAT_MESSAGE_ID);
		
		uriMatcher.addURI(AUTHORITY, ConversationTable.TABLE_NAME, CONVERSATION);
		uriMatcher.addURI(AUTHORITY, ConversationTable.TABLE_NAME + "/#", CONVERSATION_ID);
	}
	
	@Override
	public boolean onCreate() {
		dbHelper = ChatDbHelper.getInstance(getContext());
		
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String table = null;
		final int code = uriMatcher.match(uri);
		
		if (code == CONTACT || code == CONTACT_ID) {
			table = ContactTable.TABLE_NAME;
			if (sortOrder == null) {
				sortOrder = ContactTable.DEFAULT_SORT_ORDER;
			}
			
			if (code == CONTACT_ID) {
				selection = ContactTable._ID + "=?";
				selectionArgs = new String[] {uri.getLastPathSegment()};
			}
		} else if (code == CONTACT_REQUEST || code == CONTACT_REQUEST_ID) {
			table = ContactRequestTable.TABLE_NAME;
			if (sortOrder == null) {
				sortOrder = ContactRequestTable.DEFAULT_SORT_ORDER;
			}
			
			if (code == CONTACT_REQUEST_ID) {
				selection = ContactRequestTable._ID + "=?";
				selectionArgs = new String[] {uri.getLastPathSegment()};
			}
		} else if (code == CHAT_MESSAGE || code == CHAT_MESSAGE_ID){
			table = ChatMessageTable.TABLE_NAME;
			if (sortOrder == null) {
				sortOrder = ChatMessageTable.DEFAULT_SORT_ORDER;
			}
			
			if (code == CHAT_MESSAGE_ID) {
				selection = ChatMessageTable._ID + "=?";
				selectionArgs = new String[] {uri.getLastPathSegment()};
			}
		} else if (code == CONVERSATION || code == CONVERSATION_ID) {
			table = ConversationTable.TABLE_NAME;
			if (sortOrder == null) {
				sortOrder = ConversationTable.DEFAULT_SORT_ORDER;
			}
			
			if (code == CONVERSATION_ID) {
				selection = ConversationTable._ID + "=?";
				selectionArgs = new String[] {uri.getLastPathSegment()};
			}
		} else {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
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
			
		case CHAT_MESSAGE:
			return ChatMessageTable.CONTENT_TYPE;
			
		case CHAT_MESSAGE_ID:
			return ChatMessageTable.CONTENT_ITEM_TYPE;
			
		case CONVERSATION:
			return ConversationTable.CONTENT_TYPE;
			
		case CONVERSATION_ID:
			return ConversationTable.CONTENT_ITEM_TYPE;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri contentUri = null;
		String table = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case CONTACT:
			table = ContactTable.TABLE_NAME;
			contentUri = ContactTable.CONTENT_URI;
			break;
			
		case CONTACT_REQUEST:
			table = ContactRequestTable.TABLE_NAME;
			contentUri = ContactRequestTable.CONTENT_URI;
			break;
			
		case CHAT_MESSAGE:
			table = ChatMessageTable.TABLE_NAME;
			contentUri = ChatMessageTable.CONTENT_URI;
			break;
			
		case CONVERSATION:
			table = ConversationTable.TABLE_NAME;
			contentUri = ConversationTable.CONTENT_URI;
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		long rowId = db.insert(table, null, values);
		if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// Don't support delete
		return 0;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		String table = null;
		
		switch (uriMatcher.match(uri)) {
		case CONTACT:
			table = ContactTable.TABLE_NAME;
			break;
			
		case CONTACT_REQUEST:
			table = ContactRequestTable.TABLE_NAME;
			break;
		
		case CONTACT_REQUEST_ID:
			table = ContactRequestTable.TABLE_NAME;
			where = DatabaseUtils.concatenateWhere(ContactRequestTable._ID + " = " + ContentUris.parseId(uri), where);
			break;
			
		case CHAT_MESSAGE_ID:
			table = ChatMessageTable.TABLE_NAME;
			where = DatabaseUtils.concatenateWhere(ChatMessageTable._ID + " = " + ContentUris.parseId(uri), where);
			break;
			
		case CONVERSATION:
			table = ConversationTable.TABLE_NAME;
			break;
			
		case CONVERSATION_ID:
			table = ConversationTable.TABLE_NAME;
			where = DatabaseUtils.concatenateWhere(ConversationTable._ID + " = " + ContentUris.parseId(uri), where);
			break;
		
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = db.update(table, values, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}
	
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			db.setTransactionSuccessful();
			
			return results;
		} catch (OperationApplicationException e) {
			e.printStackTrace();
			return null;
		} finally {
			db.endTransaction();
		}
	}
}