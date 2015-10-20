package com.mstr.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mstr.letschat.R;

public class PreferenceUtils {
	public static void setLoginUser(Context context, String user, String password, String nickname) {
		String encryptedUser = AESEncryption.encrypt(context, user);
		String encryptedPassword = AESEncryption.encrypt(context, password);
		getSharedPreferences(context).edit().putString(context.getString(R.string.username_preference), encryptedUser)
				.putString(context.getString(R.string.password_preference), encryptedPassword)
				.putString(context.getString(R.string.nickname_preference), nickname).commit();
	}

	public static String getUser(Context context) {
		String encryptedUser = getSharedPreferences(context).getString(context.getString(R.string.username_preference), null);
		return encryptedUser != null ? AESEncryption.decrypt(context, encryptedUser) : null;
	}
	
	public static String getPassword(Context context) {
		String encryptedPassword = getSharedPreferences(context).getString(context.getString(R.string.password_preference), null);
		return encryptedPassword != null ? AESEncryption.decrypt(context, encryptedPassword) : null;
	}
	
	public static String getNickname(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.nickname_preference), null);
	}

	public static String getServerHost(Context context) {
		return getSharedPreferences(context).getString(context.getString(R.string.server_address_preference), null);
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}