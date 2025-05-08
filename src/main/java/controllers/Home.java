package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class Home extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Try to load an existing session
        if (SessionManager.loadSession()) {
            // Session exists, try to validate it
            User savedUser = SessionManager.getCurrentUser();
            if (savedUser != null) {
                try {
                    // Validate stored credentials
                    UserService userService = new UserService();
                    User validUser = userService.getUserById(savedUser.getId());

                    // Check if user exists and password matches (the stored password should match from the session)
                    if (validUser != null && validUser.getPassword().equals(savedUser.getPassword())) {
                        // Valid credentials, set the complete user data
                        SessionManager.setCurrentUser(validUser);

                        // Navigate to appropriate screen based on role
                        String fxmlFile;
                        if ("ROLE_ADMIN".equalsIgnoreCase(validUser.getRole())) {
                            fxmlFile = "/AfficherUser.fxml";
                        } else {
                            fxmlFile = "/accueil.fxml";
                        }

                        // Load the appropriate FXML file
                        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
                        primaryStage.setTitle("Bienvenue " + validUser.getName());
                        primaryStage.setScene(new Scene(root));
                        primaryStage.show();
                        return;
                    }
                } catch (SQLException | IOException e) {
                    System.err.println("Error during auto-login: " + e.getMessage());
                    // If there's an error, clear the session and continue to login screen
                    SessionManager.clearSession();
                }
            }
        }

        // No valid session, show the login screen
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        primaryStage.setTitle("Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}