package com.mstr.letschat.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class UserUtils {
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_NICKNAME = "nickname";
	
	public static void setLoginUser(Context context, String user, String password, String nickname) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_USER, user).
				putString(KEY_PASSWORD, password).putString(KEY_NICKNAME, nickname).commit();
	}
	
	public static String getUser(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_USER, null);
	}
	
	public static String getPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PASSWORD, null);
	}
	
	public static String getNickname(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_NICKNAME, null);
	}
}