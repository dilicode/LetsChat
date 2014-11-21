package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactRequest implements Parcelable {
	public static final int STATUS_PENDING = 1;
	public static final int STATUS_ACCPTED = 2;
	
	private String jid;
	private String nickname;
	
	private int status = STATUS_PENDING;
	
	public ContactRequest() {}
	
	public ContactRequest(String jid, String nickname) {
		this.jid = jid;
		this.nickname = nickname;
	}
	
	public ContactRequest(String jid, String nickname, int status) {
		this.jid = jid;
		this.nickname = nickname;
		this.status = status;
	}
	
	public ContactRequest(Parcel in) {
		jid = in.readString();
		nickname = in.readString();
		status = in.readInt();
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getNotificationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(nickname).append(" ").append("");
		
		return sb.toString();
	}
	
	public static final Creator<ContactRequest> CREATOR = new Creator<ContactRequest>() {
		@Override
		public ContactRequest createFromParcel(Parcel in) {
			return new ContactRequest(in);
		}
		
		public ContactRequest[] newArray(int size) {
			return new ContactRequest[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(jid);
		dest.writeString(nickname);
		dest.writeInt(status);
	}
}