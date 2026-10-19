package sanzaru.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import sanzaru.AccountType;

/** ユーザー (JSP からは ${user.displayName} のように使う) */
public class User {
	private int id;
	private String username;
	private String displayName;
	private String bio;
	private AccountType type;
	private long createdAt;
	private int followingCount;
	private int followerCount;
	private int postCount;
	private boolean followedByViewer;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }

	public String getBio() { return bio; }
	public void setBio(String bio) { this.bio = bio; }

	public AccountType getType() { return type; }
	public void setType(AccountType type) { this.type = type; }

	public long getCreatedAt() { return createdAt; }
	public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

	public int getFollowingCount() { return followingCount; }
	public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

	public int getFollowerCount() { return followerCount; }
	public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

	public int getPostCount() { return postCount; }
	public void setPostCount(int postCount) { this.postCount = postCount; }

	/** 見ている人がこのユーザーをフォローしているか */
	public boolean isFollowedByViewer() { return followedByViewer; }
	public void setFollowedByViewer(boolean followedByViewer) { this.followedByViewer = followedByViewer; }

	public String getCreatedDate() {
		return DateTimeFormatter.ofPattern("yyyy/M/d").format(Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()));
	}

	public long getAvatarHue() { return Avatar.hue(username); }
	public String getInitial() { return Avatar.initial(displayName, username); }
}
