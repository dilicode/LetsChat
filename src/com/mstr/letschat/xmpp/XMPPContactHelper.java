package com.mstr.letschat.xmpp;

import java.util.Collection;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.AppLog;

public class XMPPContactHelper implements XMPPConnectionStateListener, PacketListener {
	public static final String EXTRA_DATA_NAME_FROM = "com.mstr.letschat.From";
	public static final String EXTRA_DATA_NAME_TYPE = "com.mstr.letschat.Type";
	
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
	public void onConnected(XMPPConnection newConnection) {
		if (connection == null) {
			connection = newConnection;
			connection.addPacketListener(this, new PacketTypeFilter(Presence.class));
			roster = connection.getRoster();
			roster.addRosterListener(new RosterListener() {
				@Override
				public void entriesAdded(Collection<String> arg0) {
					AppLog.d("entriesAdded");
					
				}

				@Override
				public void entriesDeleted(Collection<String> arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void entriesUpdated(Collection<String> arg0) {
					AppLog.d("entriesUpdated");
					
				}

				@Override
				public void presenceChanged(Presence arg0) {
					AppLog.d("presenceChanged");
					
				}
			});
		}
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
		sendPresenceTo(to, new Presence(Presence.Type.subscribed));
	}
	
	private void sendPresenceTo(String to, Presence presence) throws SmackInvocationException {
		presence.setTo(to);
		try {
			connection.sendPacket(presence);
		} catch (NotConnectedException e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public void setName(String jid, String name) throws SmackInvocationException {
		RosterEntry rosterEntry = roster.getEntry(jid);
		if (rosterEntry != null) {
			try {
				rosterEntry.setName(name);
			} catch (NotConnectedException e) {
				AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
				
				throw new SmackInvocationException(e);
			}
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
	
	@Override
	public void processPacket(Packet packet) {
		Presence presence = (Presence)packet;
		String from = presence.getFrom();
		Presence.Type presenceType = presence.getType();
		
		if (presenceType.equals(Type.subscribe) || presenceType.equals(Type.subscribed)) {
			Intent intent = new Intent(MessageService.ACTION_PRESENCE_RECEIVED, null, context, MessageService.class);
			intent.putExtra(EXTRA_DATA_NAME_FROM, from);
			intent.putExtra(EXTRA_DATA_NAME_TYPE, presenceType.ordinal());
			
			context.startService(intent);
		}
	}
}