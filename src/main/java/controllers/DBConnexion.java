package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnexion {

    static String user = "root";
    static String password = ""; // Mettez votre mot de passe MySQL ici si vous en avez un
    static String url = "jdbc:mysql://localhost/edunova?serverTimezone=UTC"; // Assurez-vous que votre serveur MySQL est en cours d'exécution et que le nom de la base de données est correct
    static String driver = "com.mysql.cj.jdbc.Driver";

    public static Connection getCon() {
        Connection con = null;
        try {
            Class.forName(driver);
            try {
                con = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de la connexion à la base de données", e);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Driver JDBC introuvable", e);
        }

        return con;
    }
}
