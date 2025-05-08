package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.SessionManager;

import java.io.File;
import java.sql.SQLException;

public class ProfileDialogController {

    @FXML
    private ImageView profilePictureView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label lastnameErrorLabel;
    @FXML
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label statusMessageLabel;

    private UserService userService;
    private User currentUser;
    private String profilePicturePath;

    @FXML
    public void initialize() {
        userService = new UserService();
        loadCurrentUserData();
    }

    private void loadCurrentUserData() {
        try {
            // Get current user ID from SessionManager
            int userId = SessionManager.getCurrentUserId();
            if (userId <= 0) {
                showAlert("Error", "Cannot identify current user.", Alert.AlertType.ERROR);
                closeDialog();
                return;
            }

            // Load user data from database
            currentUser = userService.getUserById(userId);
            if (currentUser == null) {
                showAlert("Error", "User data not found.", Alert.AlertType.ERROR);
                closeDialog();
                return;
            }

            // Populate fields with user data
            nameField.setText(currentUser.getName());
            lastnameField.setText(currentUser.getLastname());
            emailField.setText(currentUser.getEmail());

            // Load profile picture if available
            if (currentUser.getProfilepic() != null && !currentUser.getProfilepic().isEmpty()) {
                File file = new File(currentUser.getProfilepic());
                if (file.exists()) {
                    profilePictureView.setImage(new Image(file.toURI().toString()));
                    profilePicturePath = currentUser.getProfilepic();
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load user data: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfilePictureUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profilePicturePath = selectedFile.getAbsolutePath();
            profilePictureView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleUpdateAction(ActionEvent event) {
        clearErrorLabels();

        if (!validateInputs()) {
            return;
        }

        try {
            // Update user data
            currentUser.setName(nameField.getText().trim());
            currentUser.setLastname(lastnameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());

            // Only update password if field is not empty
            String password = passwordField.getText().trim();
            if (!password.isEmpty()) {
                currentUser.setPassword(password);
            }

            // Update profile picture if changed
            if (profilePicturePath != null) {
                currentUser.setProfilepic(profilePicturePath);
            }

            // Save to database
            userService.update(currentUser);

            // Update session data if needed
            SessionManager.setCurrentUser(currentUser);

            // Show success message
            statusMessageLabel.setText("Profile updated successfully!");
            statusMessageLabel.setVisible(true);

            // Close dialog after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::closeDialog);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showAlert("Error", "Failed to update profile: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeDialog();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Name
        if (nameField.getText().isEmpty() || !nameField.getText().matches("^[a-zA-Z]+$")) {
            nameErrorLabel.setText("Name must not be empty and should only contain letters");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Lastname
        if (lastnameField.getText().isEmpty() || !lastnameField.getText().matches("^[a-zA-Z]+$")) {
            lastnameErrorLabel.setText("Lastname must not be empty and should only contain letters");
            lastnameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Email
        if (emailField.getText().isEmpty() || !emailField.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            emailErrorLabel.setText("Please enter a valid email address");
            emailErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Password (only if provided)
        String password = passwordField.getText().trim();
        if (!password.isEmpty() && !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            passwordErrorLabel.setText("Password must have 8+ chars with uppercase, lowercase, and digits");
            passwordErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    private void clearErrorLabels() {
        nameErrorLabel.setVisible(false);
        lastnameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        statusMessageLabel.setVisible(false);
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}