package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.util.Log;

import com.mstr.letschat.model.UserSearchResult;

public class XMPPContactHelper implements XMPPConnectionChangeListener, PacketListener {
	private static final String LOG_TAG = "XMPPContactHelper";
	
	private Context context;
	
	private Roster roster;
	private XMPPConnection connection;
	
	private static XMPPContactHelper instance;
	
	private XMPPContactHelper(Context context) {
		this.context = context;
	}
	
	public static void init(Context context) {
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
		roster.setSubscriptionMode(SubscriptionMode.manual);
		
		connection.addPacketListener(this, new PacketTypeFilter(Presence.class));
	}
	
	public boolean addContact(UserSearchResult user) {
		boolean result = true;
		
		if (roster != null) {
			String jid = user.getJid();
			
			if (!roster.contains(jid)) {
				try {
					roster.createEntry(user.getJid(), user.getName(), null);
				} catch (Exception e) {
					e.printStackTrace();
					result = false;
				}
			} else {
				RosterEntry rosterEntry = roster.getEntry(jid);
				switch (rosterEntry.getType()) {
					case from:
						result = requestSubscription(jid);
						break;
						
					case to:
						result = grantSubscription(jid);
						break;
						
					case none:
						result = grantSubscription(jid) && requestSubscription(jid);
						break;
					
					case both:
					default:
						break;
				}
				
				Log.d(LOG_TAG, "addContact, jid, type, " + jid + " " + rosterEntry.getType().name());
			}
		}
		
		return result;
	}
	
	public boolean grantSubscription(String jid) {
		return sendPresenceTo(jid, new Presence(Presence.Type.subscribed));
	}
	
	private boolean requestSubscription(String jid) {
		return sendPresenceTo(jid, new Presence(Presence.Type.subscribe));
	}
	
	private boolean sendPresenceTo(String to, Presence presence) {
		presence.setTo(to);
		try {
			connection.sendPacket(presence);
			
			return true;
		} catch (NotConnectedException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	@Override
	public void processPacket(Packet packet) throws NotConnectedException {
		Presence presence = (Presence)packet;
		String fromJID = StringUtils.parseBareAddress(presence.getFrom());
		
		RosterEntry rosterEntry = roster.getEntry(fromJID);
		ItemType type = null;
		if (rosterEntry != null) {
			type = rosterEntry.getType();
		}
		
		Log.d(LOG_TAG, "roster type, " + fromJID + " " + type == null ? "null" : type.name());
		
		Presence.Type presenceType = presence.getType();
		if (presenceType.equals(Presence.Type.subscribe)) {
			grantSubscription(fromJID);
			
			Log.d(LOG_TAG, "grant to, " + fromJID);
			
			// subscribe to jid if has not
			if (type == null || (type != ItemType.to && type != ItemType.both)) {
				requestSubscription(fromJID);
				
				Log.d(LOG_TAG, "request, " + fromJID);
			}
		} else if (presenceType.equals(Presence.Type.subscribed)) {
			Log.d(LOG_TAG, "subscribed, " + type.name());
		}
	}
}