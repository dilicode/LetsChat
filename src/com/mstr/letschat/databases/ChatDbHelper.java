package com.mstr.letschat.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDbHelper extends SQLiteOpenHelper {
	 public static final int DATABASE_VERSION = 1;
	 public static final String DATABASE_NAME = "chat.db";
	 
	 public ChatDbHelper(Context context) {
		 super(context, DATABASE_NAME, null, DATABASE_VERSION);
		 ContactTableHelper.init(this);
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		ContactTableHelper.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ContactTableHelper.onUpgrade(db);
		
		onCreate(db);
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}