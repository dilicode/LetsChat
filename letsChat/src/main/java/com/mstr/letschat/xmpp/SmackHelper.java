package com.mstr.letschat.xmpp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.model.SubscribeInfo;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.FileUtils;
import com.mstr.letschat.utils.NetworkUtils;
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
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

	private FileTransferManager fileTransferManager;

	private PingManager pingManager;

	private long lastPing = new Date().getTime();

	public static final String ACTION_CONNECTION_CHANGED = "com.mstr.letschat.intent.action.CONNECTION_CHANGED";
	public static final String EXTRA_NAME_STATE = "com.mstr.letschat.State";
	
	private SmackHelper(Context context) {
		this.context = context;
		
		smackAndroid = SmackAndroid.init(context);
		
		messagePacketListener = new MessagePacketListener(context);
		presencePacketListener = new PresencePacketListener(context);
		
		SmackConfiguration.setDefaultPacketReplyTimeout(20 * 1000);
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);

		ProviderManager.addExtensionProvider(UserLocation.ELEMENT_NAME, UserLocation.NAMESPACE, new LocationMessageProvider());
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
	
	public void sendChatMessage(String to, String body, PacketExtension packetExtension) throws SmackInvocationException {
		Message message = new Message(to, Message.Type.chat);
		message.setBody(body);
		if (packetExtension != null) {
			message.addExtension(packetExtension);
		}
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

				startReconnectIfNecessary();
				
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
			//processOfflineMessages();
			
			try {
				con.sendPacket(new Presence(Presence.Type.available));
			} catch (NotConnectedException e) {}
			
			contactHelper = new SmackContactHelper(context, con);
			vCardHelper = new SmackVCardHelper(context, con);
			fileTransferManager = new FileTransferManager(con);
			OutgoingFileTransfer.setResponseTimeout(30000);
			addFileTransferListener();

			pingManager = PingManager.getInstanceFor(con);
			pingManager.registerPingFailedListener(new PingFailedListener() {
				@Override
				public void pingFailed() {
					// Note: remember that maybeStartReconnect is called from a different thread (the PingTask) here, it may causes synchronization problems
					long now = new Date().getTime();
					if (now - lastPing > 30000) {
						Log.e(LOG_TAG, "Ping failure, reconnect");
						startReconnectIfNecessary();
						lastPing = now;
					} else {
						Log.e(LOG_TAG, "Ping failure reported too early. Skipping this occurrence.");
					}
				}
			});

			con.addPacketListener(messagePacketListener, new MessageTypeFilter(Message.Type.chat));
			con.addPacketListener(presencePacketListener, new PacketTypeFilter(Presence.class));
			con.addConnectionListener(createConnectionListener());
			
			setState(State.CONNECTED);

			broadcastState(State.CONNECTED);

			MessageService.reconnectCount = 0;
		}
	}

	private void broadcastState(State state) {
		Intent intent = new Intent(ACTION_CONNECTION_CHANGED);
		intent.putExtra(EXTRA_NAME_STATE, state.toString());
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
				startReconnectIfNecessary();
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
				Log.e(LOG_TAG, "connection closed due to error ", arg0);

				startReconnectIfNecessary();
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
	
	private void startReconnectIfNecessary() {
		cleanupConnection();
		
		setState(State.WAITING_TO_CONNECT);

		if (NetworkUtils.isNetworkConnected(context)) {
			context.startService(new Intent(MessageService.ACTION_RECONNECT, null, context, MessageService.class));
		}
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
		if (vCardHelper == null) {
			throw new SmackInvocationException("server not connected");
		}
		return vCardHelper.loadStatus();
	}
	
	public VCard loadVCard(String jid) throws SmackInvocationException {
		if (vCardHelper == null) {
			throw new SmackInvocationException("server not connected");
		}

		return vCardHelper.loadVCard(jid);
	}
	
	public VCard loadVCard() throws SmackInvocationException {
		if (vCardHelper == null) {
			throw new SmackInvocationException("server not connected");
		}

		return vCardHelper.loadVCard();
	}
	
	public void saveStatus(String status) throws SmackInvocationException {
		if (vCardHelper == null) {
			throw new SmackInvocationException("server not connected");
		}

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

	public void sendImage(File file, String to) throws SmackInvocationException {
		if (fileTransferManager == null || !isConnected()) {
			throw new SmackInvocationException("server not connected");
		}

		String fullJid = to + "/" + RESOURCE_PART;
		OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(fullJid);
		try {
			transfer.sendFile(file, file.getName());
		} catch (SmackException e) {
			Log.e(LOG_TAG, "send file error");
			throw new SmackInvocationException(e);
		}

		while(!transfer.isDone()) {
			if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error)
					|| transfer.getStatus().equals(Status.cancelled)){
				throw new SmackInvocationException("send file error, " + transfer.getError());
			}
		}

		Log.d(LOG_TAG, "send file status: " + transfer.getStatus());
		if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error)
				|| transfer.getStatus().equals(Status.cancelled)){
			throw new SmackInvocationException("send file error, " + transfer.getError());
		}
	}

	private void addFileTransferListener() {
		fileTransferManager.addFileTransferListener(new FileTransferListener() {
			public void fileTransferRequest(final FileTransferRequest request) {
				new Thread() {
					@Override
					public void run() {
						IncomingFileTransfer transfer = request.accept();
						String fileName = String.valueOf(System.currentTimeMillis());
						File file = new File(FileUtils.getReceivedImagesDir(context), fileName + FileUtils.IMAGE_EXTENSION);
						try {
							transfer.recieveFile(file);
						} catch (SmackException e) {
							Log.e(LOG_TAG, "receive file error", e);
							return;
						}

						while (!transfer.isDone()) {
							if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error)
									|| transfer.getStatus().equals(Status.cancelled)){
								Log.e(LOG_TAG, "receive file error, " + transfer.getError());
								return;
							}
						}

						// start service to save the image to sqlite
						if (transfer.getStatus().equals(Status.complete)) {
							Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
							intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, StringUtils.parseBareAddress(request.getRequestor()));
							intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.image_message_body));
							intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH, file.getAbsolutePath());
							intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_IMAGE);

							context.startService(intent);
						}

					}
				}.start();
			}
		});
	}

	public void onDestroy() {
		cleanupConnection();
		
		smackAndroid.onDestroy();
	}
	
	public static enum State {
		CONNECTING,
		
		CONNECTED,
		
		DISCONNECTED,
		
		// this is a state that client is trying to reconnect to server
		WAITING_TO_CONNECT,
		
		WAITING_FOR_NETWORK;
	}
}