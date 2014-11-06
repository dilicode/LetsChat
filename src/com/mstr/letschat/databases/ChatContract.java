package com.mstr.letschat.databases;

import android.provider.BaseColumns;

public final class ChatContract {
	private ChatContract() {}

	public static abstract class ContactTableEntry implements BaseColumns {
		public static final String TABLE_NAME = "contact";
		public static final String COLUMN_NAME_ENTRY_ID = "id";
		public static final String COLUMN_NAME_USER = "user";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_OWNER = "owner";
	}
}