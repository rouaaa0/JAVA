package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.User;

public class DetailUserController {

    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel; // Updated to use a Label instead of ComboBox
    @FXML private TextField profilePathField;
    @FXML private ImageView profileImage;

    @FXML
    public void initialize() {
        // No additional setup needed for roleLabel
    }

    public void initUserData(User user) {
        // Populate fields with user data
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        emailField.setText(user.getEmail());

        // Set the role label based on the user's role
        if (user.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            roleLabel.setText("Admin");
        } else if (user.getRole().equalsIgnoreCase("ROLE_USER")) {
            roleLabel.setText("User");
        } else {
            roleLabel.setText("Unknown"); // Fallback for unexpected roles
        }

        profilePathField.setText(user.getProfilepic());

        // Load profile picture or fallback to default
        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            try {
                profileImage.setImage(new Image("file:" + user.getProfilepic()));
            } catch (Exception e) {
                profileImage.setImage(new Image("/images/default-profile.png"));
            }
        } else {
            profileImage.setImage(new Image("/images/default-profile.png"));
        }
    }

    @FXML
    private void handleBackAction() {
        // Close the current stage
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}