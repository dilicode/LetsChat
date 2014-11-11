package com.mstr.letschat.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class UserUtils {
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	
	public static void setUser(Context context, String user) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_USER, user).commit();
	}
	
	public static String getUser(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_USER, null);
	}
	
	public static void setPassword(Context context, String password) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_PASSWORD, password).commit();
	}
	
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PASSWORD, null);
	}
}