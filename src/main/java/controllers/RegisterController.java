package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label lastnameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Label messageLabel;

    private boolean passwordVisible = false;
    private final UserService userService = new UserService();

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        clearErrors(); // Clear previous error messages

        String name = nameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String email = emailField.getText().trim();
        String rawPassword = passwordVisible ? visiblePasswordField.getText() : passwordField.getText().trim();

        boolean hasError = false;

        // Validate name
        if (name.isEmpty()) {
            nameErrorLabel.setText("Name is required");
            hasError = true;
        } else if (name.length() < 2) {
            nameErrorLabel.setText("Minimum 2 characters");
            hasError = true;
        }

        // Validate lastname
        if (lastname.isEmpty()) {
            lastnameErrorLabel.setText("Last name is required");
            hasError = true;
        } else if (lastname.length() < 2) {
            lastnameErrorLabel.setText("Minimum 2 characters");
            hasError = true;
        }

        // Validate email
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email is required");
            hasError = true;
        } else if (!isValidEmail(email)) {
            emailErrorLabel.setText("Invalid email format");
            hasError = true;
        } else {
            try {
                if (userService.emailExists(email)) {
                    emailErrorLabel.setText("Email already in use");
                    hasError = true;
                }
            } catch (SQLException e) {
                emailErrorLabel.setText("Database error");
                hasError = true;
            }
        }

        // Validate password
        if (rawPassword.isEmpty()) {
            passwordErrorLabel.setText("Password is required");
            hasError = true;
        } else if (rawPassword.length() < 6) {
            passwordErrorLabel.setText("Minimum 6 characters");
            hasError = true;
        }

        if (hasError) return;

        try {
            // Hash password and create a new user
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            User user = new User(name, lastname, email, hashedPassword, "ROLE_USER", null); // Role and profilepic are default

            // Save user
            userService.add(user);

            // Redirect to login page and prefill email/password
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();
            loginController.prefillLogin(email, rawPassword);

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("An error occurred during registration", "error");
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(regex, email);
    }

    private void clearErrors() {
        nameErrorLabel.setText("");
        lastnameErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        messageLabel.setText("");
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error while navigating back: " + e.getMessage());
        }
    }
}