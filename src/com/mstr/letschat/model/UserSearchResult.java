package com.mstr.letschat.model;

import com.mstr.letschat.R;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSearchResult implements Parcelable {
	public static final int STATUS_CONTACT = 1;
	public static final int STATUS_NOT_CONTACT = 2;
	public static final int STATUS_WAITING_FOR_ACCEPTANCE = 3;
	
	private String user;
	private String nickname;
	private String jid;
	
	private int status = STATUS_NOT_CONTACT;
	
	public UserSearchResult(String user, String nickname, String jid) {
		this.user = user;
		this.nickname = nickname;
		this.jid = jid;
	}
	
	public UserSearchResult() {}
	
	private UserSearchResult(Parcel in) {
		user = in.readString();
		nickname = in.readString();
		jid = in.readString();
		status = in.readInt();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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
	
	public int getStatusString() {
		return status == STATUS_CONTACT ? R.string.added : 
			(status == STATUS_NOT_CONTACT ? R.string.add : R.string.wait_for_acceptance);
	}
	
	public boolean canAddToContact() {
		return status == STATUS_NOT_CONTACT;
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
		
		if (!(o instanceof UserSearchResult)) {
			return false;
		}
		
		return jid.equals(((UserSearchResult)o).jid);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user);
		dest.writeString(nickname);
		dest.writeString(jid);
		dest.writeInt(status);
	}
	
	public static final Parcelable.Creator<UserSearchResult> CREATOR = new Parcelable.Creator<UserSearchResult>() {
		@Override
		public UserSearchResult createFromParcel(Parcel source) {
			return new UserSearchResult(source);
		}

		@Override
		public UserSearchResult[] newArray(int size) {
			return new UserSearchResult[size];
		}
	};
}