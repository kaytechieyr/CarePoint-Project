package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public final class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/db_carepoint";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException e) {
            connection = null;
            JOptionPane.showMessageDialog(null,
                    "Connection check failed: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database db_carepoint successfully.");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL Driver not found: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            connection = null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            connection = null;
        }

        return connection;
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to close database connection: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                connection = null;
            }
        }
    }
}
