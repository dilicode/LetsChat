package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.XMPPConnection;

public interface XMPPConnectionStateListener {
	public void onConnected(XMPPConnection newConnection);
}
