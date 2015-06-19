package com.mstr.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserUtils {
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_NICKNAME = "nickname";
	private static final String KEY_STATUS = "status";
	
	public static void setLoginUser(Context context, String user, String password, String nickname) {
		getSharedPreferences(context).edit().putString(KEY_USER, user).
				putString(KEY_PASSWORD, password).putString(KEY_NICKNAME, nickname).commit();
	}
	
	public static void setStatus(Context context, String status) {
		getSharedPreferences(context).edit().putString(KEY_STATUS, status).commit();
	}
	
	public static String getUser(Context context) {
		return getSharedPreferences(context).getString(KEY_USER, null);
	}
	
	public static String getPassword(Context context) {
		return getSharedPreferences(context).getString(KEY_PASSWORD, null);
	}
	
	public static String getNickname(Context context) {
		return getSharedPreferences(context).getString(KEY_NICKNAME, null);
	}
	
	public static String getStatus(Context context) {
		return getSharedPreferences(context).getString(KEY_STATUS, null);
	}
	
	private static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}