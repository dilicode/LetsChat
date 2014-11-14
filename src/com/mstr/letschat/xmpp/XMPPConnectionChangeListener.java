package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.XMPPConnection;

public interface XMPPConnectionChangeListener {
	public void onConnectionChange(XMPPConnection newConnection);
}
