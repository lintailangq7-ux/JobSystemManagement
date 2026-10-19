package sanzaru;

import java.nio.file.Files;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import sanzaru.dao.Db;

/** アプリ起動時に MySQL へ接続し、テーブルを準備する */
@WebListener
public class AppListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();
		try {
			Config config = Config.load(ctx);
			Files.createDirectories(config.uploadDir);
			Db.init(config);
			ctx.log("三猿SNS 起動: MySQL " + config.dbHost + "/" + config.dbName + ", 画像・音声の保存先 " + config.uploadDir);
		} catch (Exception e) {
			// 接続できなくてもアプリは起動させ、画面にエラーを表示する (AppFilter 参照)
			ctx.log("MySQL に接続できませんでした", e);
			ctx.setAttribute("startupError", e.getMessage());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Tomcat を止めたときに MySQL ドライバのスレッドが残らないようにする
		for (Driver d : Collections.list(DriverManager.getDrivers())) {
			if (d.getClass().getClassLoader() == getClass().getClassLoader()) {
				try {
					DriverManager.deregisterDriver(d);
				} catch (Exception ignored) {
				}
			}
		}
		AbandonedConnectionCleanupThread.checkedShutdown();
	}
}
