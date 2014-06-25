package com.util;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;
import org.h2.tools.Server;

/**
 * @author Alex Maven
 * @date 2014年6月10日
 * @Company:
 */
public class DbUtil {
	private Server DB_H2;
	public static DbUtil dbUtil;
	private Connection connection;

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * 构造方法：新建数据连接，初始化数据库
	 */
	public DbUtil() {
		DB_Init();
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:mem:metro", "sa",
					"");
			RunScript.execute("jdbc:h2:mem:metro", "sa", "", "./database.sql",
					Charset.forName("UTF-8"), true);
		} catch (Exception e) {
			System.out.println("数据库初始化出错:" + e);
		}
	}

	/**
	 * @Title: getInstance
	 * @Description:单例模式实例化DbUtil类
	 * @return
	 */
	public static DbUtil getInstance() {
		if (dbUtil == null) {
			dbUtil = new DbUtil();
		}
		return dbUtil;
	}

	/**
	 * @Title: DB_Init
	 * @Description:启动H2数据库
	 */
	public void DB_Init() {
		try {
			DB_H2 = Server.createTcpServer(new String[] { "-tcpPort", "8844" })
					.start();
			System.out.println("数据库启动成功");
		} catch (SQLException e) {
			System.out.println("数据库启动出错:" + e);
		}
	}

	/**
	 * @Title: DB_Stop
	 * @Description:关闭H2数据库
	 */
	public void DB_Stop() {
		if (DB_H2 != null) {
			DB_H2.stop();
			System.out.println("数据库关闭成功");
		}
	}
}
