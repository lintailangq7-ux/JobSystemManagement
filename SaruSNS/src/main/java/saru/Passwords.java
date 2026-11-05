package saru;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * パスワードのハッシュ化（ソルトなし・SHA-256）
 */
public final class Passwords {
	private Passwords() {}

	/** パスワードからハッシュを作る */
	public static String hash(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	/** パスワードが一致するか確認 */
	public static boolean verify(String password, String expectedHash) {
		if (password == null || password.isEmpty() || expectedHash == null) return false;
		String actual = hash(password);
		return MessageDigest.isEqual(
				actual.getBytes(StandardCharsets.US_ASCII),
				expectedHash.getBytes(StandardCharsets.US_ASCII));
	}
}