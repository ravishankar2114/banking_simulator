package com.yourbank.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DatabaseUtil {

    private static Connection connection = null;
    private static Properties properties = new Properties();


    static {
        try (InputStream input = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties")) {

            if (input == null) {
                System.err.println("FATAL ERROR: db.properties file not found in resources folder.");
                throw new RuntimeException("db.properties file not found in resources folder");
            }


            properties.load(input);


            Class.forName(properties.getProperty("DB_DRIVER"));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load database properties or driver");
        }
    }


    private DatabaseUtil() {}


    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    properties.getProperty("DB_URL"),
                    properties.getProperty("DB_USER"),
                    properties.getProperty("DB_PASSWORD")
            );
        }
        return connection;
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}