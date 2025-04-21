package controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.User;
import service.UserService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.File;
import java.sql.SQLException;

public class AfficherUserController {

    // FXML Fields
    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ImageView profilePicturePreview;
    @FXML private FlowPane cardContainer;
    @FXML private PasswordField passwordField;
    // Service and Data
    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private String profilePicturePath; // To store the selected profile picture path
    private User selectedUser; // To track the currently selected user

    @FXML
    public void initialize() {
        // Initialize ComboBox options
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");

        // Load users and display as cards
        loadUsers();
    }

    private void loadUsers() {
        try {
            userList.clear();
            userList.addAll(userService.select());
            displayUsersAsCards(); // Display the users as cards
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayUsersAsCards() {
        cardContainer.getChildren().clear(); // Clear existing cards

        for (User user : userList) {
            // Create a VBox for each user card
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #004AAD; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 5; -fx-padding: 5; -fx-background-color: #FFFFFF;");
            card.setPrefWidth(105); // Set card width

            // Add user details to the card
            Label nameLabel = new Label("Name: " + user.getName());
            nameLabel.setStyle("-fx-font-weight:bold ; -fx-text-fill: #004AAD;");

            Label lastnameLabel = new Label("Lastname: " + user.getLastname());
            lastnameLabel.setStyle("-fx-text-fill: #004AAD;");

            Label emailLabel = new Label("Email: " + user.getEmail());
            emailLabel.setStyle("-fx-text-fill: #004AAD;");

            Label roleLabel = new Label("Role: " + user.getRole());
            roleLabel.setStyle("-fx-text-fill: #004AAD;");

            // Add profile picture
            ImageView profilePic = new ImageView();
            profilePic.setFitWidth(50);
            profilePic.setFitHeight(50);
            profilePic.setPreserveRatio(true);
            if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
                File file = new File(user.getProfilepic());
                if (file.exists()) {
                    profilePic.setImage(new Image(file.toURI().toString()));
                }
            }

            // Add components to the card
            card.getChildren().addAll(profilePic, nameLabel, lastnameLabel, emailLabel, roleLabel);

            // Enable card click to select user
            card.setOnMouseClicked(event -> populateFormWithUser(user));

            // Add the card to the FlowPane
            cardContainer.getChildren().add(card);
        }
    }

    private void populateFormWithUser(User user) {
        // Populate the form fields with the selected user's data
        selectedUser = user; // Set the selected user
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());

        // Load the profile picture preview
        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            File file = new File(user.getProfilepic());
            if (file.exists()) {
                profilePicturePreview.setImage(new Image(file.toURI().toString()));
            } else {
                profilePicturePreview.setImage(null);
            }
        }
    }

    @FXML
    public void handleProfilePictureUpload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profilePicturePath = selectedFile.getAbsolutePath();
            profilePicturePreview.setImage(new Image(selectedFile.toURI().toString()));
        } else {
            showAlert("No File Selected", "Please select an image file for the profile picture.", Alert.AlertType.WARNING);
        }
    }

  /*  @FXML
    public void handleAddAction(ActionEvent actionEvent) {
        // Validate Name
        if (nameField.getText().isEmpty() || !nameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Name must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Lastname
        if (lastnameField.getText().isEmpty() || !lastnameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Lastname must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Email
        if (emailField.getText().isEmpty() || !emailField.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showAlert("Validation Error", "Email must not be empty and should be a valid email address (e.g., user@example.com).", Alert.AlertType.WARNING);
            return;
        }

        // Validate Role
        if (roleComboBox.getValue() == null) {
            showAlert("Validation Error", "Role must be selected.", Alert.AlertType.WARNING);
            return;
        }
        String hashedPassword;
        try {
            hashedPassword = hashPassword(passwordField.getText().trim());
        } catch (NoSuchAlgorithmException e) {
            showAlert("Error", "Could not hash the password: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Create a new User object
        User user = new User(
                nameField.getText().trim(),
                lastnameField.getText().trim(),
                emailField.getText().trim(),
                hashedPassword,
                roleComboBox.getValue(),
                profilePicturePath // Use the selected profile picture path
        );

        try {
            // Add the user to the database
            userService.add(user);

            // Add the user to the list and refresh cards
            userList.add(user);
            displayUsersAsCards();

            // Clear the form
            clearForm();

            // Show success message
            showAlert("Success", "User added successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not add user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // Utility method to hash passwords
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Use SHA-256 for hashing
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b)); // Convert each byte to a hex value
        }
        return sb.toString();
    }*/


    @FXML
    public void handleAddAction(ActionEvent actionEvent) {
        // Validate Name
        if (nameField.getText().isEmpty() || !nameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Name must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Lastname
        if (lastnameField.getText().isEmpty() || !lastnameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Lastname must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Email
        if (emailField.getText().isEmpty() || !emailField.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showAlert("Validation Error", "Email must not be empty and should be a valid email address (e.g., user@example.com).", Alert.AlertType.WARNING);
            return;
        }

        // Validate Role
        if (roleComboBox.getValue() == null) {
            showAlert("Validation Error", "Role must be selected.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Password
        String password = passwordField.getText().trim();
        if (!isValidPassword(password)) {
            showAlert("Validation Error", """
                Password must meet the following criteria:
                - At least 8 characters
                - At least one uppercase letter
                - At least one lowercase letter
                - At least one digit
          
                """, Alert.AlertType.WARNING);
            return;
        }

        // Hash the password
        String hashedPassword;
        try {
            hashedPassword = hashPassword(password);
        } catch (NoSuchAlgorithmException e) {
            showAlert("Error", "Could not hash the password: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Create a new User object
        User user = new User(
                nameField.getText().trim(),
                lastnameField.getText().trim(),
                emailField.getText().trim(),
                hashedPassword, // Store the hashed password
                roleComboBox.getValue(),
                profilePicturePath // Use the selected profile picture path
        );

        try {
            // Add the user to the database
            userService.add(user);

            // Add the user to the list and refresh cards
            userList.add(user);
            displayUsersAsCards();

            // Clear the form
            clearForm();

            // Show success message
            showAlert("Success", "User added successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not add user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Utility method to validate password
    private boolean isValidPassword(String password) {
        // Regex for password validation
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        return password.matches(passwordRegex);
    }

    // Utility method to hash passwords
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Use SHA-256 for hashing
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b)); // Convert each byte to a hex value
        }
        return sb.toString();
    }
    @FXML
    public void handleUpdateAction(ActionEvent actionEvent) {
        if (selectedUser == null) {
            showAlert("No User Selected", "Please select a user to update.", Alert.AlertType.WARNING);
            return;
        }

        // Update only fields that are not empty
        if (!nameField.getText().trim().isEmpty()) selectedUser.setName(nameField.getText().trim());
        if (!lastnameField.getText().trim().isEmpty()) selectedUser.setLastname(lastnameField.getText().trim());
        if (!emailField.getText().trim().isEmpty()) selectedUser.setEmail(emailField.getText().trim());
        if (roleComboBox.getValue() != null) selectedUser.setRole(roleComboBox.getValue());
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) selectedUser.setProfilepic(profilePicturePath);

        // Save the updated user
        try {
            userService.update(selectedUser);
            displayUsersAsCards();
            clearForm();
            showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteAction(ActionEvent actionEvent) {
        if (selectedUser == null) {
            showAlert("No User Selected", "Please select a user to delete.", Alert.AlertType.WARNING);
            return;
        }

        // Confirm deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmationAlert.setContentText("User: " + selectedUser.getName() + " " + selectedUser.getLastname());
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(selectedUser.getId());
                    userList.remove(selectedUser);
                    displayUsersAsCards();
                    clearForm();
                    showAlert("Success", "User deleted successfully!", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void clearForm() {
        nameField.clear();
        lastnameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        profilePicturePreview.setImage(null);
        profilePicturePath = null;
        selectedUser = null;
        passwordField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}