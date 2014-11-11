package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable {
	public static final int TYPE_INCOMING = 1;
	public static final int TYPE_OUTGOING = 2;
	
	private String jid;
	private String body;
	private long time;
	private int type;
	
	public ChatMessage() {}
	
	public ChatMessage(Parcel in) {
		jid = in.readString();
		body = in.readString();
		time = in.readLong();
		type = in.readInt();
	}
	
	public static ChatMessage newIncomingMessage() {
		ChatMessage message = new ChatMessage();
		message.type = TYPE_INCOMING;
		message.time = System.currentTimeMillis();
		
		return message;
	}
	
	public static ChatMessage newOutgoingMessage() {
		ChatMessage message = new ChatMessage();
		message.type = TYPE_OUTGOING;
		message.time = System.currentTimeMillis();
		
		return message;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(jid);
		dest.writeString(body);
		dest.writeLong(time);
		dest.writeInt(type);
	}
	
	public static Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
		public ChatMessage createFromParcel(Parcel source) {
			return new ChatMessage(source);
		}
		
		public ChatMessage[] newArray(int size) {
			return new ChatMessage[size];
		}
	};
}