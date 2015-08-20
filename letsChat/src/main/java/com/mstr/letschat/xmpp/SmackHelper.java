package com.mstr.letschat.xmpp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.SubscribeInfo;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.PreferenceUtils;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;

public class SmackHelper {
	private static final String LOG_TAG = "SmackHelper";
	
	private static final int PORT = 5222;
	
	public static final String RESOURCE_PART = "Smack";

	private XMPPConnection con;
	
	private ConnectionListener connectionListener;
	
	private Context context;
	
	private State state;
	
	private PacketListener messagePacketListener;
	
	private PacketListener presencePacketListener;
	
	private SmackAndroid smackAndroid;
	
	private static SmackHelper instance;
	
	private SmackContactHelper contactHelper;
	
	private SmackVCardHelper vCardHelper;
	
	private SmackHelper(Context context) {
		this.context = context;
		
		smackAndroid = SmackAndroid.init(context);
		
		messagePacketListener = new MessagePacketListener(context);
		presencePacketListener = new PresencePacketListener(context);
		
		SmackConfiguration.setDefaultPacketReplyTimeout(20 * 1000);
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
	}
	
	public static synchronized SmackHelper getInstance(Context context) {
		if (instance == null) {
			instance = new SmackHelper(context.getApplicationContext());
		}
		
		return instance;
	}
	
	public void setState(State state) {
		if (this.state != state) {
			Log.d(LOG_TAG, "enter state: " + state.name());
			
			this.state = state;
		}
	}
	
	public void signupAndLogin(String user, String password, String nickname, byte[] avatar) throws SmackInvocationException {
		connect();
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("name", nickname);
		try {
			AccountManager.getInstance(con).createAccount(user, password, attributes);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
		
		login(user, password);
		
		vCardHelper.save(nickname, avatar);
	}
	
	public void sendChatMessage(String to, String body) throws SmackInvocationException {
		Message message = new Message(to, Message.Type.chat);
		message.setBody(body);
		try {
			con.sendPacket(message);
		} catch (NotConnectedException e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public List<RosterEntry> getRosterEntries() {
		List<RosterEntry> result = new ArrayList<RosterEntry>();
		
		Roster roster = con.getRoster();
		Collection<RosterGroup> groups = roster.getGroups();
		for (RosterGroup group : groups) {
			result.addAll(group.getEntries());
		}
		
		return result;
	}
	
	public UserProfile search(String username) throws SmackInvocationException {
		String name = StringUtils.parseName(username);
		String jid = null;
		if (name == null || name.trim().length() == 0) {
			jid = username + "@" + con.getServiceName();
		} else {
			jid = StringUtils.parseBareAddress(username);
		}

		if (vCardHelper == null) {
			return null;
		}

		VCard vCard = vCardHelper.loadVCard(jid);
		String nickname = vCard.getNickName();
		
		return nickname == null ? null : new UserProfile(jid, vCard);
	}
	
	public String getNickname(String jid) throws SmackInvocationException {
		VCard vCard = vCardHelper.loadVCard(jid);
		
		return vCard.getNickName();
	}
	
	private void connect() throws SmackInvocationException {
		if (!isConnected()) {
			setState(State.CONNECTING);
			
			if (con == null) {
				con = createConnection();
			}
			
			try {
				con.connect();
			} catch(Exception e) {
				Log.e(LOG_TAG, String.format("Unhandled exception %s", e.toString()), e);
				
				startReconnect();
				
				throw new SmackInvocationException(e);
			}
		}
	}
	
	@SuppressLint("TrulyRandom")
	private XMPPConnection createConnection() {
		ConnectionConfiguration config = new ConnectionConfiguration(PreferenceUtils.getServerHost(context), PORT);
		
		SSLContext sc = null;
		MemorizingTrustManager mtm = null;
		try {
			mtm = new MemorizingTrustManager(context);
			sc = SSLContext.getInstance("TLS");
			sc.init(null, new X509TrustManager[] { mtm }, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (KeyManagementException e) {
			throw new IllegalStateException(e);
		}
			
		config.setCustomSSLContext(sc);
		config.setHostnameVerifier(mtm.wrapHostnameVerifier(new org.apache.http.conn.ssl.StrictHostnameVerifier()));
		config.setSecurityMode(SecurityMode.required);
		config.setReconnectionAllowed(false);
		config.setSendPresence(false);
		
		return new XMPPTCPConnection(config);
	}
	
	public void cleanupConnection() {
		if (con != null) {
			con.removePacketListener(messagePacketListener);
			con.removePacketListener(presencePacketListener);
			
			if (connectionListener != null) {
				con.removeConnectionListener(connectionListener);
			}
		}
		
		if (isConnected()) {
			try {
				con.disconnect();
			} catch (NotConnectedException e) {}
		}
	}
	
	private void onConnectionEstablished() {
		if (state != State.CONNECTED) {
			processOfflineMessages();
			
			try {
				con.sendPacket(new Presence(Presence.Type.available));
			} catch (NotConnectedException e) {}
			
			contactHelper = new SmackContactHelper(context, con);
			vCardHelper = new SmackVCardHelper(context, con);
			
			con.addPacketListener(messagePacketListener, new MessageTypeFilter(Message.Type.chat));
			con.addPacketListener(presencePacketListener, new PacketTypeFilter(Presence.class));
			con.addConnectionListener(createConnectionListener());
			
			setState(State.CONNECTED);
		}
	}
	
	public void login(String username, String password) throws SmackInvocationException {
		connect();
		
		try {
			if (!con.isAuthenticated()) {
				con.login(username, password, RESOURCE_PART);
			}
			
			onConnectionEstablished();
		} catch(Exception e) {
			SmackInvocationException exception = new SmackInvocationException(e);
			// this is caused by wrong username/password, do not reconnect
			if (exception.isCausedBySASLError()) {
				cleanupConnection();
			} else {
				startReconnect();
			}
			
			throw exception;
		}
	}
	
	public String getLoginUserNickname() throws SmackInvocationException {
		try {
			return AccountManager.getInstance(con).getAccountAttribute("name");
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	private void processOfflineMessages() {
		Log.i(LOG_TAG, "Begin retrieval of offline messages from server");
		
		OfflineMessageManager offlineMessageManager = new OfflineMessageManager(con);
		try {
			if (!offlineMessageManager.supportsFlexibleRetrieval()) {
				Log.d(LOG_TAG, "Offline messages not supported");
				return;
			}
			
			List<Message> msgs = offlineMessageManager.getMessages();
			for (Message msg : msgs) {
				Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
				intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, StringUtils.parseBareAddress(msg.getFrom()));
				intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, msg.getBody());
            	
            	context.startService(intent);
            }
			
			offlineMessageManager.deleteMessages();
		} catch (Exception e) {
			Log.e(LOG_TAG, "handle offline messages error ", e);
		}
		
		Log.i(LOG_TAG, "End of retrieval of offline messages from server");
	}
	
	private ConnectionListener createConnectionListener() {
		connectionListener = new ConnectionListener() {
			@Override
			public void authenticated(XMPPConnection arg0) {}

			@Override
			public void connected(XMPPConnection arg0) {}

			@Override
			public void connectionClosed() {
				Log.e(LOG_TAG, "connection closed");
			}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				// it may be due to network is not available or server is down, update state to WAITING_TO_CONNECT
				// and schedule an automatic reconnect
				Log.e(LOG_TAG, "xmpp disconnected due to error ", arg0);
				
				startReconnect();
			}

			@Override
			public void reconnectingIn(int arg0) {}

			@Override
			public void reconnectionFailed(Exception arg0) {}

			@Override
			public void reconnectionSuccessful() {}
		};
		
		return connectionListener;
	}
	
	private void startReconnect() {
		cleanupConnection();
		
		setState(State.WAITING_TO_CONNECT);
		
		context.startService(new Intent(MessageService.ACTION_RECONNECT, null, context, MessageService.class));
	}
	
	private boolean isConnected() {
		return con != null && con.isConnected();
	}
	
	public void onNetworkDisconnected() {
		setState(State.WAITING_FOR_NETWORK);
	}
	
	public void requestSubscription(String to, String nickname) throws SmackInvocationException {
		contactHelper.requestSubscription(to, nickname);
	}
	
	public void approveSubscription(String to, String nickname, boolean shouldRequest) throws SmackInvocationException {
		contactHelper.approveSubscription(to);
		
		if (shouldRequest) {
			requestSubscription(to, nickname);
		}
	}
	
	public void delete(String jid) throws SmackInvocationException {
		contactHelper.delete(jid);
	}
	
	public String loadStatus() throws SmackInvocationException {
		return vCardHelper.loadStatus();
	}
	
	public VCard loadVCard(String jid) throws SmackInvocationException {
		return vCardHelper.loadVCard(jid);
	}
	
	public VCard loadVCard() throws SmackInvocationException {
		return vCardHelper.loadVCard();
	}
	
	public void saveStatus(String status) throws SmackInvocationException {
		vCardHelper.saveStatus(status);
		
		contactHelper.broadcastStatus(status);
	}
	
	public SubscribeInfo processSubscribe(String from) throws SmackInvocationException {
		SubscribeInfo result = new SubscribeInfo();
		
		RosterEntry rosterEntry = contactHelper.getRosterEntry(from);
		ItemType rosterType = rosterEntry != null ? rosterEntry.getType() : null;
		
		if (rosterEntry == null || rosterType == ItemType.none) {
			result.setType(SubscribeInfo.TYPE_WAIT_FOR_APPROVAL);
			result.setNickname(getNickname(from));
		} else if (rosterType == ItemType.to) {
			result.setType(SubscribeInfo.TYPE_APPROVED);
			result.setNickname(rosterEntry.getName());
		
			approveSubscription(from, null, false);
		}
		
		result.setFrom(from);
		return result;
	}
	
	public void onDestroy() {
		cleanupConnection();
		
		smackAndroid.onDestroy();
	}
	
	private static enum State {
		CONNECTING,
		
		CONNECTED,
		
		DISCONNECTED,
		
		// this is a state that client is trying to reconnect to server
		WAITING_TO_CONNECT,
		
		WAITING_FOR_NETWORK;
	}
}