package com.mstr.letschat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.service.MessageService;

public class MessagePacketListener implements PacketListener {
	public static final String EXTRA_DATA_NAME_Message_BODY = "com.mstr.letschat.Body";
	
	private Context context;
	
	public MessagePacketListener(Context context) {
		this.context = context;
	}
	
	@Override
	public void processPacket(Packet packet) {
		Message msg = (Message)packet;
		
		Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, StringUtils.parseBareAddress(msg.getFrom()));
		intent.putExtra(EXTRA_DATA_NAME_Message_BODY, msg.getBody());
		context.startService(intent);
	}
}