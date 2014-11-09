package com.mstr.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserUtils {
	private static final String KEY_USER = "user";
	
	public static void setUser(Context context, String user) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPreferences.edit().putString(KEY_USER, user).commit();
	}
	
	public static String getUser(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_USER, null);
	}
}