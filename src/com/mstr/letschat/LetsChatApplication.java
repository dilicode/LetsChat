package com.mstr.letschat;

import android.app.Application;

import com.mstr.letschat.utils.XMPPUtils;

public class LetsChatApplication extends Application {
	public void onCreate() {
		super.onCreate();
		
		XMPPUtils.init(this);
	}
}