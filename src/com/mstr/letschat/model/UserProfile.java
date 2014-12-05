package com.mstr.letschat.model;

import org.jivesoftware.smack.util.StringUtils;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProfile implements Parcelable {
	public static final int STATUS_CONTACT = 1;
	public static final int STATUS_NOT_CONTACT = 2;
	public static final int STATUS_UNKNOWN = 3;
	
	private String nickname;
	private String jid;
	
	private int status = STATUS_UNKNOWN;
	
	public UserProfile(String nickname, String jid) {
		this.nickname = nickname;
		this.jid = jid;
	}
	
	public UserProfile(String nickname, String jid, int status) {
		this.nickname = nickname;
		this.jid = jid;
		this.status = status;
	}
	
	public static UserProfile newInstance(Contact contact) {
		return new UserProfile(contact.getNickname(), contact.getJid(), STATUS_CONTACT);
	}
	
	private UserProfile(Parcel in) {
		nickname = in.readString();
		jid = in.readString();
		status = in.readInt();
	}

	public String getUserName() {
		return StringUtils.parseName(jid);
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public boolean isContact() {
		return status == STATUS_CONTACT;
	}
	
	public void markAsContact() {
		status = STATUS_CONTACT;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof UserProfile)) {
			return false;
		}
		
		return jid.equals(((UserProfile)o).jid);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nickname);
		dest.writeString(jid);
		dest.writeInt(status);
	}
	
	public static final Parcelable.Creator<UserProfile> CREATOR = new Parcelable.Creator<UserProfile>() {
		@Override
		public UserProfile createFromParcel(Parcel source) {
			return new UserProfile(source);
		}

		@Override
		public UserProfile[] newArray(int size) {
			return new UserProfile[size];
		}
	};
}