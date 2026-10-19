package sanzaru;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

import jakarta.servlet.ServletContext;

/**
 * 接続先などの設定。
 * 環境変数 (DB_HOST など) → WEB-INF/db.properties → 既定値 の順に探す。
 */
public final class Config {
	public final String dbHost;
	public final int dbPort;
	public final String dbUser;
	public final String dbPassword;
	public final String dbName;
	public final Path uploadDir;

	private Config(Properties p) {
		dbHost = pick("DB_HOST", p, "db.host", "localhost");
		dbPort = Integer.parseInt(pick("DB_PORT", p, "db.port", "3306"));
		dbUser = pick("DB_USER", p, "db.user", "root");
		dbPassword = pick("DB_PASSWORD", p, "db.password", "");
		dbName = pick("DB_NAME", p, "db.name", "hitokoto_sns");
		uploadDir = Path.of(pick("UPLOAD_DIR", p, "upload.dir",
				Path.of(System.getProperty("user.home"), "sanzaru-sns-uploads").toString()));
		if (!dbName.matches("[A-Za-z0-9_]+")) {
			throw new IllegalArgumentException("db.name に使えない文字があります: " + dbName);
		}
	}

	public static Config load(ServletContext ctx) throws IOException {
		Properties p = new Properties();
		try (InputStream in = ctx.getResourceAsStream("/WEB-INF/db.properties")) {
			if (in != null) p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		}
		return new Config(p);
	}

	private static String pick(String env, Properties p, String key, String def) {
		String v = System.getenv(env);
		if (v != null && !v.isEmpty()) return v;
		v = p.getProperty(key);
		if (v != null && !v.isBlank()) return v.trim();
		return def;
	}
}
