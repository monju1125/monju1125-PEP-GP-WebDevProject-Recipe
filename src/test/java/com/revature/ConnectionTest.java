package com.revature;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.revature.util.ConnectionUtil;

public class ConnectionTest {

	@Test
	public void getConnectionTest() throws SQLException {
		Connection connection = new ConnectionUtil().getConnection();
		assertNotNull(connection, () -> "Connection should not be null");
		connection.close();
	}

}
