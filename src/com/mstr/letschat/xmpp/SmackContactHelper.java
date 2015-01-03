package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.utils.AppLog;

public class SmackContactHelper {
	private XMPPConnection con;
	
	private Roster roster;
	
	private PacketListener presencePacketListener;
	
	public SmackContactHelper(Context context, XMPPConnection con) {
		this.con = con;
		
		roster = con.getRoster();
		presencePacketListener = new PresencePacketListener(context);
		con.addPacketListener(presencePacketListener, new PacketTypeFilter(Presence.class));
	}
	
	public void addContact(String to, String nickname) throws SmackInvocationException {
		try {
			roster.createEntry(to, nickname, null);
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public void approveSubscription(String to) throws SmackInvocationException {
		sendPresence(to, new Presence(Presence.Type.subscribed));
	}
	
	public void sendPresence(String to, Presence presence) throws SmackInvocationException {
		if (to != null) {
			presence.setTo(to);
		}
		
		try {
			con.sendPacket(presence);
		} catch (NotConnectedException e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public void delete(String jid) throws SmackInvocationException {
		RosterEntry rosterEntry = roster.getEntry(jid);
		if (rosterEntry != null) {
			try {
				roster.removeEntry(rosterEntry);
			} catch (Exception e) {
				AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
				
				throw new SmackInvocationException(e);
			}
		}
	}
	
	public RosterEntry getRosterEntry(String from) {
		return roster.getEntry(from);
	}
}