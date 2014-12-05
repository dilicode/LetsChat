package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.AppLog;

public class XMPPContactHelper implements XMPPConnectionStateListener, PacketListener {
	public static final String EXTRA_DATA_NAME_FROM = "com.mstr.letschat.From";
	public static final String EXTRA_DATA_NAME_TYPE = "com.mstr.letschat.Type";
	public static final String EXTRA_DATA_NAME_EXTENSION_DATA = "com.mstr.letschat.ExtensionData";
	
	public static final String EXTENSION_ELEMENT_NAME = "presence";
	public static final String EXTENSION_NAMESPACE = "custom";
	public static final String EXTENSION_NAME_NEED_APPROVAL = "needapproval";
	public static final String EXTENSION_NAME_FROM_NICKNAME = "fromnickname";
	
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
		
	}
	
	public void requestSubscription(String to, String nickname, boolean needApproval) throws SmackInvocationException {
		sendPresenceTo(to, createPresence(Presence.Type.subscribe, needApproval, nickname));
	}
	
	public void approveSubscription(String to) throws SmackInvocationException {
		sendPresenceTo(to, new Presence(Presence.Type.subscribed));
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
	
	private Presence createPresence(Presence.Type type, Boolean needApproval, String nickname) {
		DefaultPacketExtension packetExtension = new DefaultPacketExtension(EXTENSION_ELEMENT_NAME, EXTENSION_NAMESPACE);
		packetExtension.setValue(EXTENSION_NAME_NEED_APPROVAL, needApproval ? String.valueOf(true) : String.valueOf(false));
		packetExtension.setValue(EXTENSION_NAME_FROM_NICKNAME, nickname);
		
		Presence presence = new Presence(type);
		presence.addExtension(packetExtension);
		
		return presence;
	}
	
	private PresenceExtensionData getPresenceData(Presence presence) {
		PresenceExtensionData data = new PresenceExtensionData();
		
		DefaultPacketExtension extension = (DefaultPacketExtension)presence.getExtension(EXTENSION_ELEMENT_NAME, EXTENSION_NAMESPACE);
		if (extension != null) {
			data.needApproval = Boolean.valueOf(extension.getValue(EXTENSION_NAME_NEED_APPROVAL));
			data.fromNickname = extension.getValue(EXTENSION_NAME_FROM_NICKNAME);
		}
		
		return data;
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
			intent.putExtra(EXTRA_DATA_NAME_TYPE, presenceType.ordinal());
			
			// if it is subscribe request, we have a couple of additional values in packet extension
			if (presenceType.equals(Type.subscribe)) {
				intent.putExtra(EXTRA_DATA_NAME_EXTENSION_DATA, getPresenceData(presence));
			}
			
			context.startService(intent);
		}
	}
	
	public static final class PresenceExtensionData implements Parcelable {
		private boolean needApproval;
		private String fromNickname;
		
		public PresenceExtensionData() {}
		
		private PresenceExtensionData(Parcel in) {
			needApproval = in.readByte() != 0;
			fromNickname = in.readString();
		}

		public String getFromNickname() {
			return fromNickname;
		}

		public boolean needApproval() {
			return needApproval;
		}

		public void setNeedApproval(boolean needApproval) {
			this.needApproval = needApproval;
		}

		public void setFromNickname(String fromNickname) {
			this.fromNickname = fromNickname;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeByte((byte)(needApproval ? 1 : 0));
			dest.writeString(fromNickname);
		}
		
		public static final Creator<PresenceExtensionData> CREATOR = new Creator<PresenceExtensionData>() {
			@Override
			public PresenceExtensionData createFromParcel(Parcel source) {
				return new PresenceExtensionData(source);
			}
			
			@Override
			public PresenceExtensionData[] newArray(int size) {
				return new PresenceExtensionData[size];
			}
		};
	}

	@Override
	public void onLogin(XMPPConnection newConnection) {
		connection = newConnection;
		connection.addPacketListener(this, new PacketTypeFilter(Presence.class));
		//roster = newConnection.getRoster();
		
	}
}