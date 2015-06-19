package com.mstr.letschat.databases;

import android.net.Uri;
import android.provider.BaseColumns;

import com.mstr.letschat.providers.CustomProvider;

public final class ChatContract {
	private ChatContract() {}

	public static abstract class ContactTable implements BaseColumns {
		public static final String TABLE_NAME = "contact";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_STATUS = "status";
		
		public static final String DEFAULT_SORT_ORDER = "nickname COLLATE LOCALIZED ASC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + CustomProvider.AUTHORITY + "/contact");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CustomProvider.AUTHORITY + ".contact";	
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CustomProvider.AUTHORITY + ".contact";
	}
	
	public static abstract class ChatMessageTable implements BaseColumns {
		public static final String TABLE_NAME = "message";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_MESSAGE = "message";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_STATUS = "status";
		
		public static final String DEFAULT_SORT_ORDER = "_id ASC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + CustomProvider.AUTHORITY + "/message");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CustomProvider.AUTHORITY + ".message";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CustomProvider.AUTHORITY + ".message";
	}
	
	public static abstract class ContactRequestTable implements BaseColumns {
		public static final String TABLE_NAME = "contactrequest";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_STATUS = "status";
		
		public static final String DEFAULT_SORT_ORDER = "_id DESC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + CustomProvider.AUTHORITY + "/contactrequest");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CustomProvider.AUTHORITY + ".contactrequest";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CustomProvider.AUTHORITY + ".contactrequest";
	}
	
	public static abstract class ConversationTable implements BaseColumns {
		public static final String TABLE_NAME = "conversation";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_LATEST_MESSAGE = "latestmessage";
		public static final String COLUMN_NAME_UNREAD = "unread";
		public static final String COLUMN_NAME_TIME = "time";
		
		public static final String DEFAULT_SORT_ORDER = "time DESC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + CustomProvider.AUTHORITY + "/conversation");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CustomProvider.AUTHORITY + ".conversation";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CustomProvider.AUTHORITY + ".conversation";
	}
}