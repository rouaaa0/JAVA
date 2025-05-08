package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox rememberMeCheckBox; // Add this in your FXML

    @FXML
    private void handleLogin(ActionEvent event) {
        // Retrieve user input
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate input fields
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            // Create a UserService instance and attempt login
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                // Successful login
                SessionManager.setCurrentUser(user); // This now saves the session automatically

                // Check the user's role and navigate accordingly
                String role = user.getRole();
                String fxmlFile;
                if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                    fxmlFile = "/AfficherUser.fxml"; // Admin view
                } else if ("ROLE_USER".equalsIgnoreCase(role)) {
                    fxmlFile = "/accueil.fxml"; // User view
                } else {
                    errorLabel.setText("Rôle inconnu !");
                    return;
                }

                // Load the appropriate FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();

                // Update the stage with the new scene
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Bienvenue " + user.getName());
                stage.show();
            } else {
                // Login failed
                errorLabel.setText("Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            errorLabel.setText("Erreur SQL lors de la connexion.");
            e.printStackTrace();
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }

    /**
     * Redirects the user to the registration page when the 'Créer un compte' hyperlink is clicked.
     *
     * @param event The ActionEvent triggered by the hyperlink click.
     */
    public void handleRegisterLinkAction(ActionEvent event) {
        try {
            // Load the registration FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();

            // Navigate to the registration page
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur lors de la redirection vers la page d'inscription : " + e.getMessage());
        }
    }

    /**
     * Prefills the login form with provided email and password.
     * This is used after successful registration to simplify user login.
     *
     * @param email    The email to prefill.
     * @param password The plain text password to prefill.
     */
    public void prefillLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
    }

    /**
     * Redirige l'utilisateur vers la page de récupération de mot de passe.
     *
     * @param event L'événement déclenché par le clic sur le lien
     */
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            // Chargement du fichier FXML pour la récupération de mot de passe
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent root = loader.load();

            // Navigation vers la page de récupération de mot de passe
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Récupération de mot de passe");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Erreur lors du chargement de la page de récupération.");
            e.printStackTrace();
        }
    }
}