package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;

public class SmackVCardHelper {
	public static final String FIELD_STATUS = "status";
	
	private Context context;
	private XMPPConnection con;
	
	public SmackVCardHelper(Context context, XMPPConnection con) {
		this.context = context;
		this.con = con;
	}
	
	public void save(String nickname, byte[] avatar) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.setNickName(nickname);
			if (avatar != null) {
				vCard.setAvatar(avatar);
			}
			vCard.setField(FIELD_STATUS, context.getString(R.string.default_status));
			vCard.save(con);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public void saveStatus(String status) throws SmackInvocationException {
		VCard vCard = loadVCard();
		vCard.setField(FIELD_STATUS, status);
		
		try {
			vCard.save(con);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public String loadStatus() throws SmackInvocationException {
		return loadVCard().getField(FIELD_STATUS);
	}
	
	public VCard loadVCard(String jid) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.load(con, jid);
			
			return vCard;
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public VCard loadVCard() throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.load(con);
			return vCard;
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
 }