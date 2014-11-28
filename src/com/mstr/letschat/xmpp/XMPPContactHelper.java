package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.AppLog;

public class XMPPContactHelper implements XMPPConnectionChangeListener, PacketListener {
	public static final String EXTRA_DATA_NAME_PRESENCE_TYPE = "com.mstr.letschat.PresenceType";
	public static final String EXTRA_DATA_NAME_FROM = "com.mstr.letschat.From";
	public static final String EXTRA_DATA_NAME_ORIGIN = "com.mstr.letschat.Origin";
	
	public static final String EXTENSION_ELEMENT_NAME = "presence";
	public static final String EXTENSION_NAMESPACE = "letschat";
	public static final String EXTENSION_NAME_ORIGIN = "origin";
	
	private Context context;
	
	private Roster roster;
	private XMPPConnection connection;
	
	private static XMPPContactHelper instance;
	
	private XMPPContactHelper(Context context) {
		this.context = context;
	}
	
	public static void init(Context context) {
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
		
		if (instance == null) {
			instance = new XMPPContactHelper(context);
		}
	}
	
	public static XMPPContactHelper getInstance() {
		return instance;
	}

	@Override
	public void onConnectionChange(XMPPConnection newConnection) {
		connection = newConnection;
		roster = newConnection.getRoster();
		
		connection.addPacketListener(this, new PacketTypeFilter(Presence.class));
	}
	
	public void requestSubscription(String to, String origin) throws SmackInvocationException {
		sendPresenceTo(to, createPresence(Presence.Type.subscribe, origin));
	}
	
	public void grantSubscription(String to, String origin) throws SmackInvocationException {
		sendPresenceTo(to, createPresence(Presence.Type.subscribed, origin));
	}
	
	public void requestSubscription(String origin) throws SmackInvocationException {
		requestSubscription(origin, origin);
	}
	
	public void grantSubscription(String origin) throws SmackInvocationException {
		grantSubscription(origin, origin);
	}
	
	private void sendPresenceTo(String to, Presence presence) throws SmackInvocationException {
		presence.setTo(to);
		try {
			connection.sendPacket(presence);
		} catch (NotConnectedException e) {
			AppLog.e(e, "Unhandled exception %s", e.toString());
			
			throw new SmackInvocationException(e);
		}
	}
	
	public void setName(String jid, String name) throws SmackInvocationException {
		RosterEntry rosterEntry = roster.getEntry(jid);
		if (rosterEntry != null) {
			try {
				rosterEntry.setName(name);
			} catch (NotConnectedException e) {
				AppLog.e(e, "Unhandled exception %s", e.toString());
				
				throw new SmackInvocationException(e);
			}
		}
	}
	
	private Presence createPresence(Presence.Type type, String origin) {
		DefaultPacketExtension packetExtension = new DefaultPacketExtension(EXTENSION_ELEMENT_NAME, EXTENSION_NAMESPACE);
		packetExtension.setValue(EXTENSION_NAME_ORIGIN, origin);
		
		Presence presence = new Presence(type);
		presence.addExtension(packetExtension);
		
		return presence;
	}
	
	private String getOrigin(Presence presence) {
		DefaultPacketExtension extension = (DefaultPacketExtension)presence.getExtension(EXTENSION_ELEMENT_NAME, EXTENSION_NAMESPACE);
		if (extension != null) {
			return extension.getValue(EXTENSION_NAME_ORIGIN);
		} else {
			return null;
		}
	}
	
	public void delete(String jid) throws SmackInvocationException {
		RosterEntry rosterEntry = roster.getEntry(jid);
		if (rosterEntry != null) {
			try {
				roster.removeEntry(rosterEntry);
			} catch (Exception e) {
				AppLog.e(e, "Unhandled exception %s", e.toString());
				
				throw new SmackInvocationException(e);
			}
		}
	}
	
	@Override
	public void processPacket(Packet packet) {
		Presence presence = (Presence)packet;
		String from = presence.getFrom();
		Presence.Type presenceType = presence.getType();
		
		if (presenceType.equals(Type.subscribe) || presenceType.equals(Type.subscribed)) {
			Intent intent = new Intent(MessageService.ACTION_PRESENCE_RECEIVED, null, context, MessageService.class);
			intent.putExtra(EXTRA_DATA_NAME_FROM, from);
			intent.putExtra(EXTRA_DATA_NAME_PRESENCE_TYPE, presenceType.ordinal());
			intent.putExtra(EXTRA_DATA_NAME_ORIGIN, getOrigin(presence));
			
			context.startService(intent);
		}
	}
}