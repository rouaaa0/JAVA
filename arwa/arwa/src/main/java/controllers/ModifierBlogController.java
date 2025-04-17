package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import models.Blog;
import service.BlogService;

import java.sql.SQLException;

public class ModifierBlogController {

    @FXML private TextField titleTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Blog blog;
    private Runnable refreshCallback;

    /**
     * Initializes the blog data to edit.
     * @param blog The blog object to modify.
     */
    public void initBlogData(Blog blog) {
        this.blog = blog;
        titleTextField.setText(blog.getTitle());
        descriptionTextArea.setText(blog.getDescription());
    }

    /**
     * Sets the callback to refresh the blog list after modification.
     * @param refreshCallback The callback method.
     */
    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    /**
     * Handles the save button action to update the blog.
     * @param event The ActionEvent triggered by clicking the save button.
     */
    @FXML
    private void handleSaveAction(ActionEvent event) {
        String newTitle = titleTextField.getText().trim();
        String newDescription = descriptionTextArea.getText().trim();

        // Validate input
        if (newTitle.isEmpty() && newDescription.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir au moins un champ pour effectuer une modification.", Alert.AlertType.ERROR);
            return;
        }

        // Update the blog entity
        if (!newTitle.isEmpty()) {
            blog.setTitle(newTitle);
        }
        if (!newDescription.isEmpty()) {
            blog.setDescription(newDescription);
        }

        BlogService blogService = new BlogService();
        try {
            blogService.update(blog); // Update the blog in the database
            showAlert("Succès", "Le blog a été modifié avec succès.", Alert.AlertType.INFORMATION);

            // Refresh the blog list in the main view
            if (refreshCallback != null) {
                refreshCallback.run();
            }

            // Close the window
            closeWindow();

        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the cancel button action to close the window without saving.
     * @param event The ActionEvent triggered by clicking the cancel button.
     */
    @FXML
    private void handleCancelAction(ActionEvent event) {
        closeWindow();
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Displays an alert dialog.
     * @param title The title of the alert.
     * @param message The content of the alert.
     * @param type The type of the alert.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}