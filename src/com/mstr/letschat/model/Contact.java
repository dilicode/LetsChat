package com.mstr.letschat.model;

public class Contact {
	private String user;
	private String name;
	private String jid;
	
	public Contact() {}
	
	public Contact(String user, String name, String jid) {
		this.user = user;
		this.name = name;
		this.jid = jid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	@Override
	public String toString() {
		return name;
	}
}