package sanzaru;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * パスワードのハッシュ化。
 * Node.js 版の crypto.scryptSync(password, salt, 64) と同じ値になるように
 * scrypt (N=16384, r=8, p=1) を実装している。そのため Node.js 版で登録したユーザーもログインできる。
 */
public final class Passwords {
	private static final int N = 16384;
	private static final int R = 8;
	private static final int P = 1;
	private static final int KEY_LENGTH = 64;
	private static final SecureRandom RANDOM = new SecureRandom();

	private Passwords() {}

	/** ランダムなソルト (16バイトを16進数にした32文字) */
	public static String newSalt() {
		byte[] b = new byte[16];
		RANDOM.nextBytes(b);
		return HexFormat.of().formatHex(b);
	}

	/** パスワードとソルトから 128 文字のハッシュを作る */
	public static String hash(String password, String salt) {
		byte[] key = scrypt(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(key);
	}

	public static boolean verify(String password, String salt, String expectedHash) {
		if (password == null || password.isEmpty()) return false;
		byte[] actual = hash(password, salt).getBytes(StandardCharsets.US_ASCII);
		return MessageDigest.isEqual(actual, expectedHash.getBytes(StandardCharsets.US_ASCII));
	}

	// ---------- scrypt (RFC 7914) ----------
	private static byte[] scrypt(byte[] password, byte[] salt) {
		int blockWords = 32 * R;
		byte[] b = pbkdf2(password, salt, P * 128 * R);
		int[] v = new int[blockWords * N];
		for (int i = 0; i < P; i++) {
			smix(b, i * 128 * R, v);
		}
		return pbkdf2(password, b, KEY_LENGTH);
	}

	private static void smix(byte[] b, int offset, int[] v) {
		int words = 32 * R;
		int[] x = new int[words];
		for (int k = 0; k < words; k++) {
			int o = offset + k * 4;
			x[k] = (b[o] & 0xff) | (b[o + 1] & 0xff) << 8 | (b[o + 2] & 0xff) << 16 | (b[o + 3] & 0xff) << 24;
		}
		for (int i = 0; i < N; i++) {
			System.arraycopy(x, 0, v, i * words, words);
			blockMix(x);
		}
		for (int i = 0; i < N; i++) {
			int j = x[(2 * R - 1) * 16] & (N - 1);
			for (int k = 0; k < words; k++) x[k] ^= v[j * words + k];
			blockMix(x);
		}
		for (int k = 0; k < words; k++) {
			int o = offset + k * 4;
			b[o] = (byte) x[k];
			b[o + 1] = (byte) (x[k] >>> 8);
			b[o + 2] = (byte) (x[k] >>> 16);
			b[o + 3] = (byte) (x[k] >>> 24);
		}
	}

	private static void blockMix(int[] b) {
		int[] x = new int[16];
		int[] y = new int[32 * R];
		System.arraycopy(b, (2 * R - 1) * 16, x, 0, 16);
		for (int i = 0; i < 2 * R; i++) {
			for (int k = 0; k < 16; k++) x[k] ^= b[i * 16 + k];
			salsa8(x);
			int dest = (i % 2 == 0 ? i / 2 : R + i / 2) * 16;
			System.arraycopy(x, 0, y, dest, 16);
		}
		System.arraycopy(y, 0, b, 0, y.length);
	}

	private static void salsa8(int[] b) {
		int[] x = b.clone();
		for (int i = 0; i < 8; i += 2) {
			x[4] ^= Integer.rotateLeft(x[0] + x[12], 7);
			x[8] ^= Integer.rotateLeft(x[4] + x[0], 9);
			x[12] ^= Integer.rotateLeft(x[8] + x[4], 13);
			x[0] ^= Integer.rotateLeft(x[12] + x[8], 18);
			x[9] ^= Integer.rotateLeft(x[5] + x[1], 7);
			x[13] ^= Integer.rotateLeft(x[9] + x[5], 9);
			x[1] ^= Integer.rotateLeft(x[13] + x[9], 13);
			x[5] ^= Integer.rotateLeft(x[1] + x[13], 18);
			x[14] ^= Integer.rotateLeft(x[10] + x[6], 7);
			x[2] ^= Integer.rotateLeft(x[14] + x[10], 9);
			x[6] ^= Integer.rotateLeft(x[2] + x[14], 13);
			x[10] ^= Integer.rotateLeft(x[6] + x[2], 18);
			x[3] ^= Integer.rotateLeft(x[15] + x[11], 7);
			x[7] ^= Integer.rotateLeft(x[3] + x[15], 9);
			x[11] ^= Integer.rotateLeft(x[7] + x[3], 13);
			x[15] ^= Integer.rotateLeft(x[11] + x[7], 18);
			x[1] ^= Integer.rotateLeft(x[0] + x[3], 7);
			x[2] ^= Integer.rotateLeft(x[1] + x[0], 9);
			x[3] ^= Integer.rotateLeft(x[2] + x[1], 13);
			x[0] ^= Integer.rotateLeft(x[3] + x[2], 18);
			x[6] ^= Integer.rotateLeft(x[5] + x[4], 7);
			x[7] ^= Integer.rotateLeft(x[6] + x[5], 9);
			x[4] ^= Integer.rotateLeft(x[7] + x[6], 13);
			x[5] ^= Integer.rotateLeft(x[4] + x[7], 18);
			x[11] ^= Integer.rotateLeft(x[10] + x[9], 7);
			x[8] ^= Integer.rotateLeft(x[11] + x[10], 9);
			x[9] ^= Integer.rotateLeft(x[8] + x[11], 13);
			x[10] ^= Integer.rotateLeft(x[9] + x[8], 18);
			x[12] ^= Integer.rotateLeft(x[15] + x[14], 7);
			x[13] ^= Integer.rotateLeft(x[12] + x[15], 9);
			x[14] ^= Integer.rotateLeft(x[13] + x[12], 13);
			x[15] ^= Integer.rotateLeft(x[14] + x[13], 18);
		}
		for (int i = 0; i < 16; i++) b[i] += x[i];
	}

	/** PBKDF2-HMAC-SHA256 (繰り返し回数 1 回。scrypt の中で使う) */
	private static byte[] pbkdf2(byte[] password, byte[] salt, int length) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(password, "HmacSHA256"));
			byte[] out = new byte[length];
			for (int block = 1, pos = 0; pos < length; block++) {
				mac.update(salt);
				mac.update(new byte[] { (byte) (block >>> 24), (byte) (block >>> 16), (byte) (block >>> 8), (byte) block });
				byte[] u = mac.doFinal();
				int n = Math.min(u.length, length - pos);
				System.arraycopy(u, 0, out, pos, n);
				pos += n;
			}
			return out;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
