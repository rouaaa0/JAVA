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

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

  /*  @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                // Connexion réussie :white_check_mark:
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
                Parent root = loader.load();
                SessionManager.setCurrentUser(user);

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Bienvenue " + user.getName());
                stage.show();
            } else {
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
*/
  @FXML
  private void handleLogin(ActionEvent event) {
      String email = emailField.getText().trim();
      String password = passwordField.getText().trim();

      if (email.isEmpty() || password.isEmpty()) {
          errorLabel.setText("Veuillez remplir tous les champs !");
          return;
      }

      try {
          UserService userService = new UserService();
          User user = userService.login(email, password);

          if (user != null) {
              // Successful login
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
              Parent root = loader.load();
              SessionManager.setCurrentUser(user);

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


    public void handleRegisterLinkAction(ActionEvent event) {
        try {
            // Charger le fichier FXML de la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène pour la page d'inscription
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur lors de la redirection vers la page d'inscription : " + e.getMessage());
        }
    }

    public void prefillLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
    }
}