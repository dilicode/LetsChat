package com.mstr.letschat;

import android.app.Application;

import com.mstr.letschat.xmpp.XMPPHelper;

public class LetsChatApplication extends Application {
	public void onCreate() {
		super.onCreate();
		
		XMPPHelper.init(this);
	}
}