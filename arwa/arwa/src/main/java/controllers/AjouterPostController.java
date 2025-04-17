package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Post;
import service.PostService;

import java.sql.SQLException;

public class AjouterPostController {

    @FXML private TextField contentTextField;
    @FXML private TextField imageTextField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    /**
     * Handles the save button action to create a new post.
     */
    @FXML
    private void handleSaveAction() {
        // Get input values
        String content = contentTextField.getText().trim();
        String image = imageTextField.getText().trim();

        // Validate input
        if (content.isEmpty()) {
            showAlert("Erreur", "Le contenu du post ne peut pas être vide.", Alert.AlertType.ERROR);
            return;
        }

        // Create the Post object
        Post post = new Post();
        post.setContent(content);
        post.setImage(image);

        // Save to database
        PostService postService = new PostService();
        try {
            postService.add(post); // Add the post
            showAlert("Succès", "Le post a été ajouté avec succès.", Alert.AlertType.INFORMATION);

            // Close the window
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'ajout du post: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the cancel button action to close the window.
     */
    @FXML
    private void handleCancelAction() {
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
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}