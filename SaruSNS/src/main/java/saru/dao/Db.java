package saru.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import saru.Config;

/** MySQL への接続と、テーブルの自動作成 */
public final class Db {
	private static Config config;

	// 外部キーの参照先が先に作られるよう、この順番で実行する
	// (CREATE TABLE IF NOT EXISTS なので、すでにあるテーブルは変更されない)
	private static final String[] SCHEMA = {
		"""
		CREATE TABLE IF NOT EXISTS users (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  user_mail VARCHAR(50) NOT NULL UNIQUE,
		  password VARCHAR(255) NOT NULL,
		  user_name VARCHAR(50) NOT NULL,
		  bio VARCHAR(300) NULL,
		  birthday DATE NULL,
		  icon_path VARCHAR(255) NULL,
		  account_type ENUM('mizaru','iwazaru','kikazaru') NOT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS posts (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  user_id INT NOT NULL,
		  body TEXT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
		  FOREIGN KEY (user_id) REFERENCES users(id),
		  INDEX idx_posts_created (created_at)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS media (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  post_id INT NOT NULL,
		  type ENUM('image','audio') NOT NULL,
		  file_path VARCHAR(255) NOT NULL,
		  title VARCHAR(100) NULL,
		  dhash BIGINT NULL,
		  UNIQUE KEY uq_post_type (post_id, type),
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS tags (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  name VARCHAR(50) NOT NULL UNIQUE
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS media_tags (
		  media_id INT NOT NULL,
		  tag_id INT NOT NULL,
		  source ENUM('user','auto') NOT NULL DEFAULT 'user',
		  PRIMARY KEY (media_id, tag_id),
		  FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE,
		  FOREIGN KEY (tag_id) REFERENCES tags(id)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS post_tags (
		  post_id INT NOT NULL,
		  tag_id INT NOT NULL,
		  source ENUM('user','auto') NOT NULL DEFAULT 'user',
		  PRIMARY KEY (post_id, tag_id),
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
		  FOREIGN KEY (tag_id) REFERENCES tags(id)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS media_features (
		  media_id INT NOT NULL,
		  kind VARCHAR(20) NOT NULL,
		  vec TEXT NOT NULL,
		  PRIMARY KEY (media_id, kind),
		  FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS likes (
		  user_id INT NOT NULL,
		  post_id INT NOT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
		  PRIMARY KEY (user_id, post_id),
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
		  INDEX idx_likes_post (post_id)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS bookmarks (
		  user_id INT NOT NULL,
		  post_id INT NOT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
		  PRIMARY KEY (user_id, post_id),
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS comments (
		  id INT AUTO_INCREMENT PRIMARY KEY,
		  post_id INT NOT NULL,
		  user_id INT NOT NULL,
		  body VARCHAR(500) NOT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
		  INDEX idx_comments_post (post_id, created_at)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS follows (
		  follower_id INT NOT NULL,
		  followee_id INT NOT NULL,
		  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
		  PRIMARY KEY (follower_id, followee_id),
		  CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id),
		  CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users(id),
		  CONSTRAINT chk_no_self_follow CHECK (follower_id <> followee_id),
		  INDEX idx_follows_followee (followee_id)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
		"""
		CREATE TABLE IF NOT EXISTS view_history (
		  user_id INT NOT NULL,
		  post_id INT NOT NULL,
		  viewed_at DATETIME NOT NULL,
		  PRIMARY KEY (user_id, post_id),
		  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
		  FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
		  INDEX idx_view_recent (user_id, viewed_at)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""",
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