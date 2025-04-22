package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static MyConnection instance; // Singleton instance
    private Connection cnx; // Database connection object

    // Database credentials and URL
    private final String URL = "jdbc:mysql://localhost:3306/edunova"; // Replace 'ta_base' with your database name
    private final String USER = "root"; // Replace 'root' with your MySQL username if different
    private final String PASSWORD = ""; // Replace with your MySQL password if set

    // Private constructor to prevent direct instantiation
    private MyConnection() {
        try {
            // Establish the database connection
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie !");
        } catch (SQLException e) {
            // Handle and log connection errors
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Public method to get the singleton instance of MyConnection
    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    // Method to retrieve the database connection
    public Connection getCnx() {
        return cnx;
    }
}