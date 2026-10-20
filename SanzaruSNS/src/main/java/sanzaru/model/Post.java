package sanzaru.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import sanzaru.AccountType;

/** 投稿 (コメントも投稿の一種で、replyTo に返信先の id が入る) */
public class Post {
	private int id;
	private int userId;
	private String text;
	private String imageFile;
	private String audioFile;
	private Integer replyTo;
	private long createdAt;
	private String authorUsername;
	private String authorDisplayName;
	private AccountType authorType;
	private int likeCount;
	private boolean liked;
	private int replyCount;
	private String parentUsername;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public String getText() { return text; }
	public void setText(String text) { this.text = text; }

	public String getImageFile() { return imageFile; }
	public void setImageFile(String imageFile) { this.imageFile = imageFile; }

	public String getAudioFile() { return audioFile; }
	public void setAudioFile(String audioFile) { this.audioFile = audioFile; }

	public Integer getReplyTo() { return replyTo; }
	public void setReplyTo(Integer replyTo) { this.replyTo = replyTo; }

	public long getCreatedAt() { return createdAt; }
	public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

	public String getAuthorUsername() { return authorUsername; }
	public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

	public String getAuthorDisplayName() { return authorDisplayName; }
	public void setAuthorDisplayName(String authorDisplayName) { this.authorDisplayName = authorDisplayName; }

	public AccountType getAuthorType() { return authorType; }
	public void setAuthorType(AccountType authorType) { this.authorType = authorType; }

	public int getLikeCount() { return likeCount; }
	public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

	/** 見ている人がいいねしているか */
	public boolean isLiked() { return liked; }
	public void setLiked(boolean liked) { this.liked = liked; }

	public int getReplyCount() { return replyCount; }
	public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

	/** 返信先の投稿者 (返信先が消えていれば null) */
	public String getParentUsername() { return parentUsername; }
	public void setParentUsername(String parentUsername) { this.parentUsername = parentUsername; }

	public long getAuthorAvatarHue() { return Avatar.hue(authorUsername); }
	public String getAuthorInitial() { return Avatar.initial(authorDisplayName, authorUsername); }

	/** 「3分前」のような表示 */
	public String getTimeAgo() {
		long s = (System.currentTimeMillis() - createdAt) / 1000;
		if (s < 60) return "たった今";
		if (s < 3600) return (s / 60) + "分前";
		if (s < 86400) return (s / 3600) + "時間前";
		if (s < 86400 * 7) return (s / 86400) + "日前";
		return DateTimeFormatter.ofPattern("yyyy/M/d").format(Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()));
	}
}
