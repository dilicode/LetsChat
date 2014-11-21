package com.mstr.letschat.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import android.content.Context;
import android.util.Log;

import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.tasks.CreateAccountTask.AccountCreationResult;

public class XMPPHelper {
	private static final String LOG_TAG = "XMPPHelper";
	
	private static final String HOST = "10.197.34.151";
	
	// private static final String HOST = "192.168.1.103";
	private static final int PORT = 5222;
	
	public static final String RESOURCE_PART = "Smack";

	private XMPPConnection con;
	
	private Context context;
	
	private List<XMPPConnectionChangeListener> listeners = new ArrayList<XMPPConnectionChangeListener>();
	
	private State state;
	
	private static XMPPHelper instance;
	
	private XMPPHelper(Context context) {
		SmackAndroid.init(context);
		
		this.context = context;
		
		XMPPContactHelper.init(context);
		listeners.add(XMPPContactHelper.getInstance());
	}
	
	/**
	 * This method is called when application is created, so instance is available afterwards.
	 * @param context
	 */
	public static void init(Context context) {
		if (instance == null) {
			instance = new XMPPHelper(context);
		}
	}
	
	public static XMPPHelper getInstance() {
		return instance;
	}
	
	public void setState(State state) {
		Log.d(LOG_TAG, "state: " + state.name());
		
		this.state = state;
	}
	
	public AccountCreationResult signup(String user, String name, String password) {
		connect();
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("name", name);
		try {
			AccountManager.getInstance(con).createAccount(user, password, attributes);
			
			return AccountCreationResult.SUCCESS;
		} catch (SmackException e) {
			e.printStackTrace();
		} catch(XMPPErrorException e) {
			if (e.getXMPPError().getCondition().equals(Condition.conflict)) {
				return AccountCreationResult.CONFLICT;
			}
		}
		
		return AccountCreationResult.FAILURE;
	}
	
	public boolean sendChatMessage(String to, String body) {
		Message message = new Message(to, Message.Type.chat);
		message.setBody(body);
		try {
			con.sendPacket(message);
			return true;
		} catch (NotConnectedException e) {
			e.printStackTrace();
			return false;
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
	
	public ArrayList<UserSearchResult> search(String username) {
		ArrayList<UserSearchResult> result = new ArrayList<UserSearchResult>();
		
		UserSearchManager search = new UserSearchManager(con);
		
		final String searchService = "search." + con.getServiceName();
		final String KEY_USERNAME = "Username";
		final String KEY_NAME = "Name";
		final String KEY_JID = "jid";
		try {
			Form searchForm = search.getSearchForm(searchService);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("search", username);
			answerForm.setAnswer(KEY_USERNAME, true);
			answerForm.setAnswer(KEY_NAME, true);
			
			ReportedData data = search.getSearchResults(answerForm, searchService);
			List<Row> rows = data.getRows();
			for (Row row: rows) {
				List<String> usernameValues = row.getValues(KEY_USERNAME);
				List<String> nameValues = row.getValues(KEY_NAME);
				List<String> jids = row.getValues(KEY_JID);
				
				for (int i = 0; i < jids.size(); i ++) {
					result.add(new UserSearchResult(usernameValues.get(i), nameValues.get(i), jids.get(i)));
				}
			}
			
			return result;
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public UserSearchResult searchByCompleteUsername(String username) {
		List<UserSearchResult> result = search(username);
		
		return (result != null && result.size() > 0) ? result.get(0) : null;
	}
	
	public boolean addContact(String user, String name) {
		Roster roster = con.getRoster();
		if (roster != null) {
			try {
				roster.createEntry(user, name, null);
				return true;
			} catch (NotLoggedInException e) {
				e.printStackTrace();
			} catch (NoResponseException e) {
				e.printStackTrace();
			} catch (XMPPErrorException e) {
				e.printStackTrace();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public void connect() {
		if (con == null || !con.isConnected()) {
			setState(State.CONNECTING);
			
			try {
				if (con == null) {
					ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
					//loadCA(config);
					config.setReconnectionAllowed(false);
					config.setSecurityMode(SecurityMode.disabled);
					
					con = new XMPPTCPConnection(config);
				}
				
				con.connect();
				
				setState(State.CONNECTED);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean connectAndLogin(String user, String password) {
		connect();
		
		try {
			if (!con.isAuthenticated()) {
				con.login(user, password, RESOURCE_PART);
				onConnectionEstablished();
			}
			
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void onConnectionEstablished() {
		for (XMPPConnectionChangeListener listener : listeners) {
			listener.onConnectionChange(con);
		}
	}
	
	private void loadCA(ConnectionConfiguration config){
		// Load CAs from an InputStream
		InputStream caInput = null;
		try {
			caInput = context.getAssets().open("server.crt");
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// From https://www.washington.edu/itconnect/security/ca/load-der.crt
			Certificate ca = cf.generateCertificate(caInput);
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
	
			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
	
			// Create an SSLContext that uses our TrustManager
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			
			config.setSocketFactory(sslContext.getSocketFactory());
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (caInput != null) {
				try {
					caInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addPacketListener(PacketListener packetListener) {
		if (con != null && packetListener != null) {
			con.addPacketListener(packetListener, new MessageTypeFilter(Message.Type.chat));
		}
	}
	
	private static enum State {
		CONNECTING,
		
		CONNECTED,
		
		DISCONNECTED;
	}
	
	public static String getThreadSignature(){
		Thread t = Thread.currentThread();
		long l = t.getId();
		String name = t.getName();
		long p = t.getPriority();
		String gname = t.getThreadGroup().getName();
		return (name
		+ ":(id)" + l
		+ ":(priority)" + p
		+ ":(group)" + gname);
	}
}