package com.mstr.letschat.databases;

import android.provider.BaseColumns;

public final class ChatContract {
	private ChatContract() {}

	public static abstract class ContactTableEntry implements BaseColumns {
		public static final String TABLE_NAME = "contact";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
	}
	
	public static abstract class ChatMessageTableEntry implements BaseColumns {
		public static final String TABLE_NAME = "chatmessage";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_MESSAGE = "message";
		public static final String COLUMN_NAME_TIME = "time";
	}
	
	public static abstract class IncomingContactRequestTableEntry implements BaseColumns {
		public static final String TABLE_NAME = "contactrequest";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_STATUS = "status";
	}
}