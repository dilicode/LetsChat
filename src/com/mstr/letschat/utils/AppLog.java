package com.mstr.letschat.utils;

import java.util.Locale;

import android.util.Log;

public class AppLog {
	public static String TAG = "Letschat";
	
	public static void v(String format, Object... args) {
		Log.v(TAG, buildMessage(format, args));
	}
	
	public static void d(String format, Object... args) {
		Log.d(TAG, buildMessage(format, args));
	}
	
	public static void e(String format, Object... args) {
		Log.e(TAG, buildMessage(format, args));
	}
	
	public static void e(Throwable tr, String format, Object... args) {
		Log.e(TAG, buildMessage(format, args), tr);
	}
	
	private static String buildMessage(String format, Object... args) {
		String msg = (args == null) ? format : String.format(Locale.US, format, args);
		StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
		
		String caller = "<unknown>";
		for (int i = 2; i < trace.length; i++) {
			Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(AppLog.class)) {
            	String callingClass = trace[i].getClassName();
            	callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
            	callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);
            	
            	caller = callingClass + "." + trace[i].getMethodName();
            	break;
            }
		}
		return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), caller, msg);
    }
}