package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import service.UserService;

import java.sql.SQLException;
import java.util.function.Consumer;

public class ModifierUserController {

    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField profilePathField;

    private User currentUser;
    private Consumer<Void> refreshCallback;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll("ROLE_ADMIN", "ROLE_USER");
    }

    public void initUserData(User user) {
        this.currentUser = user;
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        roleCombo.setValue(user.getRole());
        profilePathField.setText(user.getProfilepic());
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave() {
        try {
            currentUser.setName(nameField.getText());
            currentUser.setLastname(lastnameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setRole(roleCombo.getValue());
            currentUser.setProfilepic(profilePathField.getText());

            userService.update(currentUser);

            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
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