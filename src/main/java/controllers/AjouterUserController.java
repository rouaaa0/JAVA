package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import service.UserService;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class AjouterUserController implements Initializable {

    @FXML private Button saveButton; // Button for saving user
    @FXML private Button cancelButton; // Button for canceling the form
    @FXML private TextField EmailTextField;
    @FXML private TextField LastnameTextField;
    @FXML private TextField NameTextField;
    @FXML private PasswordField PasswordTextField; // Secure password input
    @FXML private TextField ProfilepicTextField;
    @FXML private ComboBox<String> RoleComboBox;

    @FXML private Label NameErrorLabel; // Error label for Name
    @FXML private Label LastnameErrorLabel; // Error label for Last Name
    @FXML private Label EmailErrorLabel; // Error label for Email
    @FXML private Label PasswordErrorLabel; // Error label for Password
    @FXML private Label RoleErrorLabel; // Error label for Role

    private Consumer<Void> refreshCallback;
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize ComboBox with roles
        RoleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");
        RoleComboBox.setValue("ROLE_USER"); // Set default role
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }

    @FXML
    void ajouterAction(ActionEvent event) {
        clearErrorLabels();

        if (!validateInputs()) {
            return; // Stop if inputs are invalid
        }

        try {
            // Hash the password before saving
            String hashedPassword = BCrypt.hashpw(PasswordTextField.getText(), BCrypt.gensalt());

            // Create the User object
            User user = new User(
                    NameTextField.getText().trim(),
                    LastnameTextField.getText().trim(),
                    EmailTextField.getText().trim(),
                    hashedPassword,
                    RoleComboBox.getValue(),
                    ProfilepicTextField.getText().trim()
            );

            // Add the user to the database
            userService.add(user);

            // Show success message
            showAlert("Succès", "Utilisateur ajouté avec succès !", AlertType.INFORMATION);

            // Clear the form
            clearForm();

            // Refresh the parent view if callback exists
            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (NameTextField.getText().trim().isEmpty()) {
            NameErrorLabel.setText("Nom requis");
            isValid = false;
        }

        if (LastnameTextField.getText().trim().isEmpty()) {
            LastnameErrorLabel.setText("Prénom requis");
            isValid = false;
        }

        String email = EmailTextField.getText().trim();
        if (email.isEmpty()) {
            EmailErrorLabel.setText("Email requis");
            isValid = false;
        } else if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            EmailErrorLabel.setText("Email invalide");
            isValid = false;
        }

        String password = PasswordTextField.getText();
        if (password.isEmpty()) {
            PasswordErrorLabel.setText("Mot de passe requis");
            isValid = false;
        } else if (password.length() < 6) {
            PasswordErrorLabel.setText("Min 6 caractères");
            isValid = false;
        }

        String role = RoleComboBox.getValue();
        if (role == null || role.trim().isEmpty()) {
            RoleErrorLabel.setText("Rôle requis");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void clearErrorLabels() {
        NameErrorLabel.setText("");
        LastnameErrorLabel.setText("");
        EmailErrorLabel.setText("");
        PasswordErrorLabel.setText("");
        RoleErrorLabel.setText("");
    }

    private void clearForm() {
        NameTextField.clear();
        LastnameTextField.clear();
        EmailTextField.clear();
        PasswordTextField.clear();
        ProfilepicTextField.clear();
        RoleComboBox.setValue("ROLE_USER");
    }

    private void closeWindow() {
        Stage stage = (Stage) NameTextField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        ajouterAction(event);
    }


}