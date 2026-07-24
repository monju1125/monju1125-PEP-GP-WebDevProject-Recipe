package com.revature.util;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;

/**
This class provides autility methods and configuration for managing database connections for an H2 database. It uses a JdbcDataSource connection pool to manage connections efficiently.

You do not need to edit this class.

 */
public class ConnectionUtil {

    // fields
	private static String url = "jdbc:h2:./h2/db;";
	private static String username = "sa";
	private static String password = "";
	private static JdbcDataSource pool = new JdbcDataSource();

	/**
	 * static initialization block to establish credentials for DataSoure Pool
	 */
	static {
		pool.setURL(url);
		pool.setUser(username);
		pool.setPassword(password);
	}

	/**
	 * @return an active connection to the database
	 */
	public Connection getConnection() {
		try {
			return pool.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
