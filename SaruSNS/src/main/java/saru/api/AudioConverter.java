package saru.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** WAV 以外の音声を、ffmpeg で 16bit・16000Hz・モノラルの WAV にして、特徴を作る */
public final class AudioConverter {
	private AudioConverter() {}
	
	public static final Set<String> AUDIO_EXT = Set.of("wav", "mp3", "m4a", "aac", "ogg");
	private static final int MAX_SECONDS = 60; // 長い音は、先頭の60秒だけ使う
	private static final long TIMEOUT_SEC = 30;
		
	/** ffmpeg の場所: -Dsaru.ffmpeg=... → 環境変数 SARU_FFMPEG → PATH の ffmpeg */
	/** ffmpeg の場所: -Dsaru.ffmpeg=... → 環境変数 SARU_FFMPEG → 開発用の既定の場所 → PATH の ffmpeg */
	private static String ffmpeg() {
		String p = System.getProperty("saru.ffmpeg");
		if (p == null || p.isBlank()) p = System.getenv("SARU_FFMPEG");
		if (p != null && !p.isBlank()) return p;

		Path dev = Path.of("C:\\dev\\ffmpeg\\bin\\ffmpeg.exe"); // 開発用のPCの既定の場所
		if (Files.isRegularFile(dev)) return dev.toString();

		return "ffmpeg";
	}
	
	/** ファイル名の拡張子(小文字)。無ければ空文字 */
	public static String extensionOf(String name) {
		if(name == null) return "";
		int i = name.lastIndexOf('.');
		return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
	}
	
	/** 音声の特徴(26個)。作れなければ null */
	public static float[] mfcc(byte[] data, String ext) {
		if(!AUDIO_EXT.contains(ext)) return null;
		if("wav".equals(ext)) {
			float[] v = AudioFeature.mfcc(data);
			if(v != null) return v;
			
		}
		byte[] wav = toWav(data, ext);
		return wav == null ? null : AudioFeature.mfcc(wav);
	}
	
	public static byte[] toWav(byte[] data, String ext) {
		Path in = null;
		Path out = null;
		try {
			in = Files.createTempFile("saru-in-", "." + ext); // ext は AUDIO_EXT のどれか
			out = Files.createTempFile("saru-out-", ".wav");
			Files.write(in, data);
			
			Process p = new ProcessBuilder(
					ffmpeg(), "-y", "-v", "error",
					"-i", in.toString(),
					"-t", String.valueOf(MAX_SECONDS),
					"-ar", "16000", "-ac" , "1", "-c:a", "pcm_s16le",
					out.toString())
					.redirectErrorStream(true)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.start();
			if (!p.waitFor(TIMEOUT_SEC, TimeUnit.SECONDS)) {
				p.destroyForcibly();
				return null;
			}
			if (p.exitValue() != 0 || Files.size(out) <= 44) return null;
			return Files.readAllBytes(out);
		} catch (IOException e) { // ffmpeg が見つからない、など
			System.err.println("ffmpeg を使えませんでした (探した場所: " + ffmpeg() + "): " + e.getMessage());
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			deleteQuietly(in);
			deleteQuietly(out);
		}
	}
	
	private static void deleteQuietly(Path p) {
		if (p == null) return;
		try {
			Files.deleteIfExists(p);
		} catch (IOException ignored) {
			// 一時ファイルが消せなくても、結果には影響しない
		}
	}
	
}
