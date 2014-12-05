package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	private int id;
	private String jid;
	private String nickname;
	
	public Contact() {}
	
	public Contact(int id, String jid, String nickname) {
		this.id = id;
		this.nickname = nickname;
		this.jid = jid;
	}
	
	public Contact(Parcel source) {
		id = source.readInt();
		jid = source.readString();
		nickname = source.readString();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String name) {
		this.nickname = name;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(jid);
		dest.writeString(nickname);
	}
	
	public static final Parcelable.Creator<Contact> CREATOR = new Creator<Contact>() {

		@Override
		public Contact createFromParcel(Parcel source) {
			return new Contact(source);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};
}