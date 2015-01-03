package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.utils.AppLog;

public class SmackVCardHelper {
	public static final String FIELD_STATUS = "status";
	
	private Context context;
	private XMPPConnection con;
	
	public SmackVCardHelper(Context context, XMPPConnection con) {
		this.context = context;
		this.con = con;
	}
	
	public void saveOnSignup(String nickname) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.setNickName(nickname);
			vCard.setField(FIELD_STATUS, context.getString(R.string.default_status));
			vCard.save(con);
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public void saveStatus(String status) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.load(con);
			vCard.setField(FIELD_STATUS, status);
			vCard.save(con);
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public String getStatus() throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.load(con);
			return vCard.getField(FIELD_STATUS);
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
	
	public VCard getVCard(String jid) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.load(con, jid);
			
			return vCard;
		} catch (Exception e) {
			AppLog.e(String.format("Unhandled exception %s", e.toString()), e);
			
			throw new SmackInvocationException(e);
		}
	}
 }