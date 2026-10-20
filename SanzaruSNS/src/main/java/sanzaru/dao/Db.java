package sanzaru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import sanzaru.Config;

/** MySQL への接続と、テーブルの自動作成 */
public final class Db {
	private static Config config;

	// Node.js 版 (server.js) と同じテーブル。どちらのアプリからも同じデータを使える
	private static final String[] SCHEMA = {
		"""
		CREATE TABLE IF NOT EXISTS users (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  username VARCHAR(20) NOT NULL UNIQUE,
		  display_name VARCHAR(50) NOT NULL,
		  bio VARCHAR(160) NOT NULL DEFAULT '',
		  type VARCHAR(16) NOT NULL,
		  salt CHAR(32) NOT NULL,
		  password_hash CHAR(128) NOT NULL,
		  created_at BIGINT NOT NULL
		)""",
		"""
		CREATE TABLE IF NOT EXISTS follows (
		  follower_id INT NOT NULL,
		  followee_id INT NOT NULL,
		  PRIMARY KEY (follower_id, followee_id),
		  KEY (followee_id),
		  FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
		  FOREIGN KEY (followee_id) REFERENCES users(id) ON DELETE CASCADE
		)""",
		// reply_to には外部キーを付けない(返信の連鎖削除はアプリ側で行う)
		"""
		CREATE TABLE IF NOT EXISTS posts (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  user_id INT NOT NULL,
		  text TEXT NOT NULL,
		  image_file VARCHAR(64) NULL,
		  image_mime VARCHAR(32) NULL,
		  audio_file VARCHAR(64) NULL,
		  audio_mime VARCHAR(32) NULL,
		  reply_to INT NULL,
		  created_at BIGINT NOT NULL,
		  KEY (user_id, created_at),
		  KEY (reply_to),
		  KEY (created_at),
		  KEY (image_file),
		  KEY (audio_file),
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
		)""",
		"""
		CREATE TABLE IF NOT EXISTS likes (
		  post_id INT NOT NULL,
		  user_id INT NOT NULL,
		  PRIMARY KEY (post_id, user_id),
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
		)""",
	};

	private Db() {}

	/** 起動時に呼ぶ。データベースとテーブルがなければ作る */
	public static void init(Config c) throws SQLException {
		config = c;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("MySQL のドライバ (WEB-INF/lib/mysql-connector-j) が見つかりません", e);
		}
		try (Connection conn = DriverManager.getConnection(url(""), c.dbUser, c.dbPassword);
				Statement st = conn.createStatement()) {
			st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + c.dbName
					+ "` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
		}
		try (Connection conn = get(); Statement st = conn.createStatement()) {
			for (String sql : SCHEMA) st.executeUpdate(sql);
		}
	}

	public static Connection get() throws SQLException {
		return DriverManager.getConnection(url(config.dbName), config.dbUser, config.dbPassword);
	}

	public static Config config() {
		return config;
	}

	private static String url(String database) {
		return "jdbc:mysql://" + config.dbHost + ":" + config.dbPort + "/" + database
				+ "?characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
	}
}
