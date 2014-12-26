package com.mstr.letschat.xmpp;

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

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.utils.UserUtils;

import de.duenndns.ssl.MemorizingTrustManager;

public class SmackHelper {
	private static final String LOG_TAG = "SmackHelper";
	
	private static final String HOST = "10.197.34.151";
	
	// private static final String HOST = "192.168.1.103";
	private static final int PORT = 5222;
	
	public static final String RESOURCE_PART = "Smack";

	private XMPPConnection con;
	
	private Roster roster;
	
	private ConnectionListener connectionListener;
	
	private Context context;
	
	private State state;
	
	private PacketListener messagePacketListener;
	private PacketListener presencePacketListener;
	
	private SmackAndroid smackAndroid;
	
	private static SmackHelper instance;
	
	private SmackHelper(Context context) {
		this.context = context;
		
		smackAndroid = SmackAndroid.init(context);
		
		messagePacketListener = new MessagePacketListener(context);
		presencePacketListener = new PresencePacketListener(context);
		
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
			switch (state) {
			case CONNECTED:
				con.addPacketListener(messagePacketListener, new MessageTypeFilter(Message.Type.chat));
				con.addPacketListener(presencePacketListener, new PacketTypeFilter(Presence.class));
				
				con.addConnectionListener(createConnectionListener());
				roster = con.getRoster();
				
				break;
			}
		}
	}
	
	public void signupAndLogin(String user, String password, String nickname) throws SmackInvocationException {
		connect();
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("name", nickname);
		try {
			AccountManager.getInstance(con).createAccount(user, password, attributes);
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
		
		login(user, password);
		
		UserUtils.setLoginUser(context, user, password, nickname);
	}
	
	public void sendChatMessage(String to, String body) throws SmackInvocationException {
		Message message = new Message(to, Message.Type.chat);
		message.setBody(body);
		try {
			con.sendPacket(message);
		} catch (NotConnectedException e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
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
	
	public ArrayList<UserProfile> search(String username) throws SmackInvocationException {
		connect();
		
		ArrayList<UserProfile> result = new ArrayList<UserProfile>();
		
		UserSearchManager search = new UserSearchManager(con);
		
		final String searchService = "search." + con.getServiceName();
		try {
			Form searchForm = search.getSearchForm(searchService);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("search", username);
			answerForm.setAnswer("Username", true);
			
			ReportedData data = search.getSearchResults(answerForm, searchService);
			List<Row> rows = data.getRows();
			for (Row row: rows) {
				List<String> nameValues = row.getValues("Name");
				List<String> jids = row.getValues("jid");
				
				for (int i = 0; i < jids.size(); i ++) {
					result.add(new UserProfile(nameValues.get(i), jids.get(i)));
				}
			}
			
			return result;
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public UserProfile searchByCompleteUsername(String username) throws SmackInvocationException {
		List<UserProfile> list = search(username);
		if (list != null) {
			for (UserProfile user : list) {
				if (user.getUserName().equals(username)) {
					return user;
				}
			}
		}
		
		return null;
	}
	
	public String getNickname(String username) {
		username = StringUtils.parseName(username);
		UserProfile userSearchResult = null;
		try {
			userSearchResult = searchByCompleteUsername(username);
		} catch (SmackInvocationException e) {}
		
		return userSearchResult != null ? userSearchResult.getNickname() : username;
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
				AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
				
				startReconnect();
				
				throw new SmackInvocationException(e);
			}
			
			setState(State.CONNECTED);
		}
	}
	
	@SuppressLint("TrulyRandom")
	private XMPPConnection createConnection() {
		ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
		
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
		
		return new XMPPTCPConnection(config);
	}
	
	public void disconnect() {
		if (isConnected()) {
			try {
				con.disconnect();
			} catch (NotConnectedException e) {}
		}
		
		setState(State.DISCONNECTED);
	}
	
	public void login(String username, String password) throws SmackInvocationException {
		connect();
		
		try {
			if (!con.isAuthenticated()) {
				con.login(username, password, RESOURCE_PART);
				
				setState(State.CONNECTED);
			}
		} catch(Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			SmackInvocationException exception = new SmackInvocationException(e);
			// this is caused by wrong username/password, do not reconnect
			if (exception.isCausedBySASLError()) {
				disconnect();
			} else {
				startReconnect();
			}
			
			throw exception;
		}
	}
	
	public String getNickname() throws SmackInvocationException {
		try {
			return AccountManager.getInstance(con).getAccountAttribute("name");
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	private ConnectionListener createConnectionListener() {
		connectionListener = new ConnectionListener() {
			@Override
			public void authenticated(XMPPConnection arg0) {}

			@Override
			public void connected(XMPPConnection arg0) {}

			@Override
			public void connectionClosed() {}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				// it may be due to network is not available or server is down, update state to WAITING_TO_CONNECT
				// and schedule an automatic reconnect
				AppLog.d("xmpp disconnected due to error ", arg0);
				
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
		setState(State.WAITING_TO_CONNECT);
		
		context.startService(new Intent(MessageService.ACTION_RECONNECT, null, context, MessageService.class));
	}
	
	private boolean isConnected() {
		return con != null && con.isConnected();
	}
	
	public void onNetworkDisconnected() {
		setState(State.WAITING_FOR_NETWORK);
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
	
	public void onDestroy() {
		disconnect();
		
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