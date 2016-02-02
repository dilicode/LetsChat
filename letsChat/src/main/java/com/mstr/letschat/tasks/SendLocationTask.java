package com.mstr.letschat.tasks;

import android.content.ContentValues;
import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.xmpp.SmackHelper;
import com.mstr.letschat.xmpp.UserLocation;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Created by dilli on 11/27/2015.
 */
public class SendLocationTask extends SendMessageTask {
    private UserLocation location;
    protected PacketExtension packetExtension;

    public SendLocationTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, UserLocation location) {
        super(listener, context, to, nickname, location.getName());

        this.location = location;
        packetExtension = location;
    }

    @Override
    protected ContentValues newMessage(long timeMillis) {
        return ChatMessageTableHelper.newLocationMessage(to, body, timeMillis, location, true);
    }

    @Override
    protected void doSend(Context context) throws SmackInvocationException {
        SmackHelper.getInstance(context).sendChatMessage(to, body, packetExtension);
    }
}