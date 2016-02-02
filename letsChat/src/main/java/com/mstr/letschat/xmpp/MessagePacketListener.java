package com.mstr.letschat.xmpp;

import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.service.MessageService;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;

import java.util.Collection;
import java.util.Iterator;

public class MessagePacketListener implements PacketListener {
	private Context context;
	
	public MessagePacketListener(Context context) {
		this.context = context;
	}
	
	@Override
	public void processPacket(Packet packet) {
		Message msg = (Message)packet;
		
		Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, StringUtils.parseBareAddress(msg.getFrom()));
		intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, msg.getBody());
		intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_PLAIN_TEXT);
		processPacketExtension(intent, msg);

		context.startService(intent);
	}

	private void processPacketExtension(Intent intent, Message msg) {
		Collection<PacketExtension> extensions = msg.getExtensions();
		if (extensions != null) {
			Iterator<PacketExtension> iterator = extensions.iterator();
			if (iterator.hasNext()) {
				PacketExtension extension = iterator.next();
				if (extension instanceof UserLocation) {
					intent.putExtra(MessageService.EXTRA_DATA_NAME_LOCATION, (UserLocation)extension);
					intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_LOCATION);
				}
			}
		}
	}
}