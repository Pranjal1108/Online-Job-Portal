package com.onlinejobportal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = System.getenv("JOBIFY_LEGACY_DB_URL");
    private static final String USER = System.getenv("JOBIFY_LEGACY_DB_USER");
    private static final String PASSWORD = System.getenv("JOBIFY_LEGACY_DB_PASSWORD");


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
