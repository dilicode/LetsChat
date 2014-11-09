package com.mstr.letschat.utils;

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
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import android.app.Application;
import android.util.Log;

import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.tasks.CreateAccountTask.AccountCreationResult;

public class XMPPUtils {
	private static final String LOG_TAG = "XMPPUtils";
	
	private static final String HOST = "10.197.34.151";
	private static final int PORT = 5223;
	
	private static XMPPConnection con;
	
	private static Application application;
	
	public static void init(Application app) {
		SmackAndroid.init(app);
		
		application = app;
	}
	
	public static boolean login(String username, String password) {
		try {
			connectIfNecessary();
			
			if (!con.isAuthenticated()) {
				con.login(username, password);
				
				Roster roster = con.getRoster();
				if (roster != null) {
					roster.addRosterListener(new RosterListener() {
						@Override
						public void entriesAdded(Collection<String> arg0) {
						}

						@Override
						public void entriesDeleted(Collection<String> arg0) {}

						@Override
						public void entriesUpdated(Collection<String> arg0) {}

						@Override
						public void presenceChanged(Presence arg0) {}
					});
				}
			}
			
			return true;
		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static AccountCreationResult createAccount(String user, String name, String password) {
		try {
			connectIfNecessary();
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("name", name);
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
	
	public static boolean sendChatMessage(String to, String body) {
		connectIfNecessary();
		
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
	
	public static List<RosterEntry> getRosterEntries() {
		connectIfNecessary();
		
		List<RosterEntry> result = new ArrayList<RosterEntry>();
		
		Roster roster = con.getRoster();
		Collection<RosterGroup> groups = roster.getGroups();
		for (RosterGroup group : groups) {
			result.addAll(group.getEntries());
		}
		
		return result;
	}
	
	public static ArrayList<UserSearchResult> search(String username) {
		connectIfNecessary();
		
		ArrayList<UserSearchResult> result = new ArrayList<UserSearchResult>();
		
		UserSearchManager search = new UserSearchManager(con);
		
		final String searchService = "search." + con.getServiceName();
		final String KEY_USERNAME = "Username";
		final String KEY_NAME = "Name";
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
				
				if (usernameValues != null && nameValues != null) {
					for (int i = 0; i < usernameValues.size() && i < nameValues.size(); i ++) {
						result.add(new UserSearchResult(usernameValues.get(i), nameValues.get(i)));
					}
				} else {
					return null;
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
	
	public static boolean addContact(String user, String name) {
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
	
	private static void connectIfNecessary() {
		try {
			if (con == null) {
				ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
				loadCA(config);
				con = new XMPPTCPConnection(config);
				con.connect();
			} else {
				if (!con.isConnected()) {
					con.connect();
				}
			}
		} catch(SmackException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(XMPPException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadCA(ConnectionConfiguration config){
		// Load CAs from an InputStream
		// (could be from a resource or ByteArrayInputStream or ...)
		InputStream caInput = null;
		try {
			caInput = application.getAssets().open("server.crt");
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