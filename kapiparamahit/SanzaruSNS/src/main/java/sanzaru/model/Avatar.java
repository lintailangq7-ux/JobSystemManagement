package sanzaru.model;

/** アイコン(頭文字と背景色)の計算 */
final class Avatar {
	private Avatar() {}

	/** ユーザー名から決まる色相 (0〜359) */
	static long hue(String username) {
		long hash = 0;
		for (int i = 0; i < username.length(); i++) {
			hash = (hash * 31 + username.charAt(i)) & 0xffffffffL;
		}
		return hash % 360;
	}

	/** 表示名の最初の1文字 (大文字にする) */
	static String initial(String displayName, String username) {
		String s = displayName == null || displayName.isEmpty() ? username : displayName;
		return new String(Character.toChars(s.codePointAt(0))).toUpperCase();
	}
}
