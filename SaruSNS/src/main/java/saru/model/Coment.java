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
	}
