package com.mstr.letschat.utils;

import android.content.ContentValues;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;

public class DatabaseUtils {
	public static final int CONTACT_REQUEST_STATUS_PENDING = 1;
	public static final int CONTACT_REQUEST_STATUS_ACCPTED = 2;
	
	public static ContentValues newContactRequestContentValues(String jid, String nickname) {
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_JID, jid);
		values.put(ContactRequestTable.COLUMN_NAME_NICKNAME, nickname);
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, CONTACT_REQUEST_STATUS_PENDING);
		
		return values;
	}
	
	public static ContentValues newContactContentValues(String jid, String nickname) {
		ContentValues values = new ContentValues();
		values.put(ContactTable.COLUMN_NAME_JID, jid);
		values.put(ContactTable.COLUMN_NAME_NICKNAME, nickname);
		
		return values;
	}
}