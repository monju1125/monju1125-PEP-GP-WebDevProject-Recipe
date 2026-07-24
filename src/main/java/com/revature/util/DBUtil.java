package com.revature.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
The DBUtil class is a utility designed to handle database setup and reset operations by reading SQL commands from a file and executing them. This can be useful in scenarios where you need to reinitialize the database with a specific schema and data set.

You do not need to edit this class.

 */
public class DBUtil {
	
	/** A mutable text object that will contain the contents of a file once initialized. */
	private static StringBuilder sqlScript = new StringBuilder();
	private static InputStream inputStream =  DBUtil.class.getResourceAsStream("/sqlScript.sql");
	

	/**
	 * static initialization block to initialize the `sqlScript` field with the content within the `sqlScript.sql` file. Any lines starting with comment syntax are ignored.
	 */
	static {
			Scanner sc = new Scanner(inputStream);
			while (sc.hasNextLine()) {
				String nextLine = sc.nextLine();
				if (!nextLine.startsWith("--")) {
					sqlScript.append(nextLine + " ");
				}
			}
			sc.close();
	}

	/**
	 * This method resets and re-initializes the database by first dropping all existing objects (tables, views, procedures, etc.) and then executing the SQL script with the `sqlScript.sql` file.
	 */
	public static void RUN_SQL() {
		try(Connection conn = new ConnectionUtil().getConnection()) {
			conn.prepareStatement("DROP ALL OBJECTS").executeUpdate();
			conn.prepareStatement(sqlScript.toString()).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
