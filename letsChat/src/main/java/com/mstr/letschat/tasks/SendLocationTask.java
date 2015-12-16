package com.mstr.letschat.tasks;

import android.content.ContentValues;
import android.content.Context;

import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.xmpp.UserLocation;

/**
 * Created by dilli on 11/27/2015.
 */
public class SendLocationTask extends SendChatMessageTask {
    private UserLocation location;

    public SendLocationTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, UserLocation location) {
        super(listener, context, to, nickname, location.getName());

        this.location = location;
        packetExtension = location;
    }

    @Override
    protected ContentValues getNewMessage(long timeMillis) {
        return ChatMessageTableHelper.newLocationMessage(to, body, timeMillis, location, true);
    }
}