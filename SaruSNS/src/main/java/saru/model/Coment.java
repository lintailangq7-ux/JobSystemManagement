package saru.model;

import saru.AccountType;

public class Coment {
	private int id ;
	private String user_mail;
	private String user_name;
	private String coment;
	private String bio;		//備考
	private AccountType type;
	private long createdAt;
	
	public Coment(int id, String user_mail, String user_name, String coment, String bio, AccountType type, long createdAt) {
		this.id = id;
		this.user_mail = user_mail;
		this.user_name = user_name;
		this.coment = coment;
		this.bio = bio;
		this.type = type;
		this.createdAt = createdAt;
		}
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getUser_mail() { return user_mail; }
	public void setUser_mail(String user_mail) { this.user_mail = user_mail; }

	public String getUser_name() { return user_name; }
	public void setUser_name(String user_name) { this.user_name = user_name; }

	public String getComent() { return coment; }
	public void setComent(String coment) { this.coment = coment; }

	public String getBio() { return bio; }
	public void setBio(String bio) { this.bio = bio; }

	public AccountType getType() { return type; }
	public void setType(AccountType type) { this.type = type; }

	public long getCreatedAt() { return createdAt; }
	public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
	
	
	
	
	
	
	}
