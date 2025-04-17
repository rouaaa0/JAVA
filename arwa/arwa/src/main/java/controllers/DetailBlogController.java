package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Blog;

public class DetailBlogController {

    @FXML private TextField blogTitleText;
    @FXML private TextArea blogDescriptionText;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Button closeButton;

    /**
     * Initialize blog data in the details view.
     * @param blog The blog object to display details for.
     */
    public void initBlogData(Blog blog) {
        blogTitleText.setText(blog.getTitle());
        blogDescriptionText.setText(blog.getDescription());
        createdAtLabel.setText(blog.getCreatedAtBlog());
        updatedAtLabel.setText(blog.getUpdatedAtBlog());
    }

    /**
     * Handle the action when the "Fermer" button is clicked.
     * Closes the current window.
     */
    @FXML
    private void handleCloseButtonAction() {
        // Close the stage (window) associated with the "Fermer" button
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}