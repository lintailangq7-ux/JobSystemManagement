package saru.api;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** ログインの目印 (トークン) をメモリに記憶する。Tomcat を再起動すると消える */
public final class ApiTokens {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final long LIFETIME_MS = 7L * 24 * 60 * 60 * 1000; // 7日
	private record Entry(int userId, long expiresAt) {}
	private static final Map<String, Entry> TOKENS = new ConcurrentHashMap<>();

	private ApiTokens() {}

	public static String issue(int userId) {
		byte[] b = new byte[32];
		RANDOM.nextBytes(b);
		String token = HexFormat.of().formatHex(b);
		TOKENS.put(token, new Entry(userId, System.currentTimeMillis() + LIFETIME_MS));
		return token;
	}

	/** 有効なら user id、無効なら null */
	public static Integer verify(String token) {
		Entry e = token == null ? null : TOKENS.get(token);
		if (e == null) return null;
		if (e.expiresAt() < System.currentTimeMillis()) {
			TOKENS.remove(token);
			return null;
		}
		return e.userId();
	}

	public static void revoke(String token) {
		if (token != null) TOKENS.remove(token);
	}
}