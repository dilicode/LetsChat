package com.mstr.letschat.utils;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.database.SQLException;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.providers.CustomProvider;

public class ProviderUtils {
	/**
	 * Save new contact to db, and update request status as accepted
	 * 
	 * @param context
	 * @param jid
	 * @param nickname
	 * @return
	 */
	public static ContentProviderResult[] addNewContact(Context context, String jid, String nickname, String status) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(ContentProviderOperation.newInsert(ContactTable.CONTENT_URI).
				withValues(ContactTableHelper.newContentValues(jid, nickname, status)).build());
		operations.add(ContentProviderOperation.newUpdate(ContactRequestTable.CONTENT_URI).
				withValues(ContactRequestTableHelper.newContentValuesWithAcceptedStatus()).
				withSelection(ContactRequestTable.COLUMN_NAME_JID + " = ?", new String[]{jid}).build());
		try {
			return context.getContentResolver().applyBatch(CustomProvider.AUTHORITY, operations);
		} catch (Exception e) {
			throw new SQLException("Failed to add contact");
		}
	}
}
