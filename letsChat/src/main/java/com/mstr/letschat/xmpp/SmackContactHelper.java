package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;

public class SmackContactHelper {
	private XMPPConnection con;
	
	private Roster roster;
	
	public SmackContactHelper(Context context, XMPPConnection con) {
		this.con = con;
	}
	
	private Roster getRoster() {
		if (roster == null) {
			roster = con.getRoster();
		}
		
		return roster;
	}
	
	public void requestSubscription(String to, String nickname) throws SmackInvocationException {
		try {
			getRoster().createEntry(to, nickname, null);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public void approveSubscription(String to) throws SmackInvocationException {
		sendPresence(to, new Presence(Presence.Type.subscribed));
	}
	
	private void sendPresence(String to, Presence presence) throws SmackInvocationException {
		if (to != null) {
			presence.setTo(to);
		}
		
		try {
			con.sendPacket(presence);
		} catch (NotConnectedException e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public void delete(String jid) throws SmackInvocationException {
		RosterEntry rosterEntry = roster.getEntry(jid);
		if (rosterEntry != null) {
			try {
				getRoster().removeEntry(rosterEntry);
			} catch (Exception e) {
				throw new SmackInvocationException(e);
			}
		}
	}
	
	public RosterEntry getRosterEntry(String from) {
		return getRoster().getEntry(from);
	}
	
	public void broadcastStatus(String status) throws SmackInvocationException {
		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus(status);
		sendPresence(null, presence);
	}
}