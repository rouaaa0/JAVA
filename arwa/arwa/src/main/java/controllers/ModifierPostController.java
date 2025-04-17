package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Post;
import service.PostService;

import java.sql.SQLException;

public class ModifierPostController {

    @FXML private TextField contentTextField;
    @FXML private TextField imageTextField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Post post;

    /**
     * Initializes the post data to edit.
     * @param post The post object to modify.
     */
    public void initPostData(Post post) {
        this.post = post;
        contentTextField.setText(post.getContent());
        imageTextField.setText(post.getImage());
    }

    /**
     * Handles the save button action to update the post.
     */
    @FXML
    private void handleSaveAction() {
        String newContent = contentTextField.getText().trim();
        String newImage = imageTextField.getText().trim();

        if (newContent.isEmpty()) {
            showAlert("Erreur", "Le contenu du post ne peut pas être vide.", Alert.AlertType.ERROR);
            return;
        }

        post.setContent(newContent);
        post.setImage(newImage);

        PostService postService = new PostService();
        try {
            postService.update(post); // Update the post in the database
            showAlert("Succès", "Le post a été modifié avec succès.", Alert.AlertType.INFORMATION);

            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la modification du post: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the cancel button action to close the window without saving.
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