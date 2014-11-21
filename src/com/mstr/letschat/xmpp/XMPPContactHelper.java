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
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mstr.letschat.service.MessageService;

public class XMPPContactHelper implements XMPPConnectionChangeListener, PacketListener {
	private static final String LOG_TAG = "XMPPContactHelper";
	
	public static final String EXTRA_DATA_NAME_CONTACT_REQUEST_TYPE = "com.mstr.letschat.ContactRequestType";
	
	public static final int CONTACT_REQUEST_TYPE_SUBSCRIBE = 1;
	public static final int CONTACT_REQUEST_TYPE_SUBSCRIBED = 2;
	
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
	
	public boolean requestSubscription(String jid, String nickname) {
		try {
			roster.createEntry(StringUtils.parseBareAddress(jid), nickname, null);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean grantSubscription(String jid) {
		return sendPresenceTo(jid, new Presence(Presence.Type.subscribed));
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
	
	public RosterEntry getEntry(String jid) {
		return roster.getEntry(jid);
	}
	
	@Override
	public void processPacket(Packet packet) throws NotConnectedException {
		Presence presence = (Presence)packet;
		String fromJid = presence.getFrom();
        Presence.Type presenceType = presence.getType();
        RosterEntry rosterEntry = roster.getEntry(fromJid);
        
        if (presenceType == Type.subscribe || presenceType == Type.subscribed) {
        	/**
        	 *  if (presenceType == Presence.Type.subscribe)
        {
            //from new user
            if (newEntry == null)
            {
                //save request locally for later accept/reject
                //later accept will send back a subscribe & subscribed presence to user with fromId
                //or accept immediately by sending back subscribe and unsubscribed right now
            }
            //from a user that previously accepted your request
            else
            {
                //send back subscribed presence to user with fromId
            }
        	 */
        	
        	
            /*if (presenceType == Type.subscribe) {
            	// from new user
            	if (rosterEntry == null) {
            		
            	}
            	
            	
            	requestType = CONTACT_REQUEST_TYPE_SUBSCRIBE;
            	
            	Log.d(LOG_TAG, "subscribe from: " + fromJid);
            } else {
            	requestType = CONTACT_REQUEST_TYPE_SUBSCRIBED;
            	
            	Log.d(LOG_TAG, "subscribed from: " + fromJid);
            }
            
        	
        	
        	
            Intent intent = new Intent(MessageService.ACTION_CONTACT_REQUEST_RECEIVED, null, context, MessageService.class);
    		intent.putExtra(MessageService.EXTRA_DATA_NAME_JID, fromJid);
    		intent.putExtra(EXTRA_DATA_NAME_CONTACT_REQUEST_TYPE, requestType);
    		
    		context.startService(intent);
        	*/
        }
	}
}