package com.mstr.letschat.model;

public class SubscribeInfo {
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_WAIT_FOR_APPROVAL = 0;
	public static final int TYPE_APPROVED = 1;
	
	private String from;
	private String nickname;
	private int type = TYPE_UNKNOWN;
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
}