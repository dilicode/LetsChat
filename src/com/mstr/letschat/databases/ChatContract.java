package com.mstr.letschat.databases;

import android.provider.BaseColumns;

public final class ChatContract {
	private ChatContract() {}

	public static abstract class ContactTableEntry implements BaseColumns {
		public static final String TABLE_NAME = "contact";
		public static final String COLUMN_NAME_CONTACT_ID = "contactid";
		public static final String COLUMN_NAME_CONTACT_NAME = "contactname";
		public static final String COLUMN_NAME_USER = "user";
	}
}