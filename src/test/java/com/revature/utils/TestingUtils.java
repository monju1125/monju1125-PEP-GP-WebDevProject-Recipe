package com.revature.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.revature.util.ConnectionUtil;

public class TestingUtils {
    @SuppressWarnings("unused")
    private static Connection connection;

    
    public static void assertCountDifference(int expectedDifference, String msg, String countSelStatement, Runnable exec) {
        int before = count(countSelStatement);
        exec.run();
        int after = count(countSelStatement);
        if (after - before != expectedDifference) {
            throw new AssertionError(msg);
        }
    }

    private static int count(String countSelStatement) {
        try(Connection connection = new ConnectionUtil().getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(countSelStatement);
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to count ingredients", ex);
        }
    }
}
