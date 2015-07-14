package com.mstr.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mstr.letschat.R;

public class PreferenceUtils {
	public static void setLoginUser(Context context, String user, String password, String nickname) {
		getSharedPreferences(context).edit().putString(context.getString(R.string.username_preference), user).
				putString(context.getString(R.string.password_preference), password)
				.putString(context.getString(R.string.nickname_preference), nickname).commit();
	}

	public static String getUser(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.username_preference), null);
	}
	
	public static String getPassword(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.password_preference), null);
	}
	
	public static String getNickname(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.nickname_preference), null);
	}

	public static String getServerHost(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.server_address_preference), null);
	}

	private static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}