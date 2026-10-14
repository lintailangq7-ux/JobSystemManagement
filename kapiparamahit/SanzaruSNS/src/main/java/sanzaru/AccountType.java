package sanzaru;

import java.security.SecureRandom;

/** アカウントタイプ: それぞれ「見られない・コメントに使えない」メディアが1つある */
public enum AccountType {
	MIZARU("mizaru", "image", "見猿", "🙈", "画像が見えず、コメントに画像を貼れません"),
	IWAZARU("iwazaru", "text", "言わ猿", "🙊", "文字が見えず、コメントに文字を打てません"),
	KIKAZARU("kikazaru", "audio", "聞か猿", "🙉", "音が聞こえず、コメントに音声を投稿できません");

	private static final SecureRandom RANDOM = new SecureRandom();

	private final String key;
	private final String blocked;
	private final String label;
	private final String emoji;
	private final String desc;

	AccountType(String key, String blocked, String label, String emoji, String desc) {
		this.key = key;
		this.blocked = blocked;
		this.label = label;
		this.emoji = emoji;
		this.desc = desc;
	}

	/** DB に保存されている文字列 (mizaru など) から探す */
	public static AccountType of(String key) {
		for (AccountType t : values()) {
			if (t.key.equals(key)) return t;
		}
		return MIZARU;
	}

	public static AccountType random() {
		return values()[RANDOM.nextInt(values().length)];
	}

	/** kind ("text" / "image" / "audio") を見られるか */
	public boolean canSee(String kind) {
		return !blocked.equals(kind);
	}

	public static String mediaLabel(String kind) {
		switch (kind) {
		case "text": return "文字";
		case "image": return "画像";
		case "audio": return "音声";
		default: return kind;
		}
	}

	public String getKey() { return key; }
	public String getBlocked() { return blocked; }
	public String getBlockedLabel() { return mediaLabel(blocked); }
	public String getLabel() { return label; }
	public String getEmoji() { return emoji; }
	public String getDesc() { return desc; }
}
