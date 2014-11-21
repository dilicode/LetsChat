package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSearchResult implements Parcelable {
	private String user;
	private String nickname;
	private String jid;
	
	private boolean added;
	
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
		added = in.readByte() != 0;
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
	
	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
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
		dest.writeByte((byte)(added ? 1 : 0));
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