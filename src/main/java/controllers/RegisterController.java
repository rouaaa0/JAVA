package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import service.UserService;
import java.util.Optional;

import java.io.File;
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
    private ImageView profilePicturePreview; // ImageView for profile picture preview
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label lastnameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label profilePicErrorLabel; // Label for profile picture errors
    @FXML
    private Label messageLabel;
    @FXML
    private Button showPasswordButton;
    @FXML
    private Button generatePasswordButton;

    private boolean passwordVisible = false;
    private final UserService userService = new UserService();
    private String profilePicturePath = null; // Path to the selected profile picture

    @FXML
    private void initialize() {
        // Initialize UI components and event handlers
        setupShowPasswordButton();
    }

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
                emailErrorLabel.setText("Database error while checking email");
                e.printStackTrace();
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

        // If validation errors exist, stop execution
        if (hasError) return;

        try {
            // Create a new user object
            User user = new User(name, lastname, email, rawPassword, "ROLE_USER", profilePicturePath);

            // Add the user to the database (UserService will handle password hashing)
            userService.add(user);

            // Redirect to login page and prefill email/password
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Prefill login details in the login page
            LoginController loginController = loader.getController();
            loginController.prefillLogin(email, rawPassword);

            // Update the stage with the login scene
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error occurred during registration", "error");
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading the login screen", "error");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("An unexpected error occurred during registration", "error");
        }
    }

    @FXML
    private void handleUploadProfilePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profilePicturePath = selectedFile.getAbsolutePath();
            profilePicturePreview.setImage(new Image(selectedFile.toURI().toString())); // Display the selected image
            profilePicErrorLabel.setText(""); // Clear any previous error message for profile picture
        } else {
            profilePicErrorLabel.setText("No file selected");
        }
    }

    @FXML
    private void handleShowPasswordAction(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Show password
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            showPasswordButton.setText("Masquer");
        } else {
            // Hide password
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            showPasswordButton.setText("Afficher");
        }
    }

    @FXML
    private void handleGeneratePasswordAction(ActionEvent event) {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog();
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String generatedPassword = result.get();

            if (passwordVisible) {
                visiblePasswordField.setText(generatedPassword);
            } else {
                passwordField.setText(generatedPassword);
            }

            passwordErrorLabel.setText("");
        }
    }

    private void setupShowPasswordButton() {
        // Add listener to sync the two password fields
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordVisible) {
                visiblePasswordField.setText(newValue);
            }
        });

        visiblePasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (passwordVisible) {
                passwordField.setText(newValue);
            }
        });
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
        profilePicErrorLabel.setText("");
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