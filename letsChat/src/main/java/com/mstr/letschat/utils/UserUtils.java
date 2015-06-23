package com.mstr.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mstr.letschat.R;

public class UserUtils {
	public static void setLoginUser(Context context, String user, String password, String nickname) {
		getSharedPreferences(context).edit().putString(context.getString(R.string.preference_key_username), user).
				putString(context.getString(R.string.preference_key_password), password)
				.putString(context.getString(R.string.preference_key_nickname), nickname).commit();
	}

	public static String getUser(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.preference_key_username), null);
	}
	
	public static String getPassword(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.preference_key_password), null);
	}
	
	public static String getNickname(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.preference_key_nickname), null);
	}

	private static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}