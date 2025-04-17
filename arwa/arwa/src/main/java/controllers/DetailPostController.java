package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Post;

import java.io.IOException;
import java.net.URI;
import java.awt.Desktop;

public class DetailPostController {

    @FXML private Label contentLabel;
    @FXML private Hyperlink imageLink;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Button closeButton;

    /**
     * Initializes the details of the selected post.
     * @param post The post object to display.
     */
    /*public void initPostData(Post post) {
        // Safely set the content
        contentLabel.setText(post.getContent() != null ? post.getContent() : "No content available");

        // Safely set the image as a hyperlink
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            imageLink.setText(post.getImage());
            imageLink.setOnAction(event -> openImageInBrowser(post.getImage()));
        } else {
            imageLink.setText("No image available");
            imageLink.setDisable(true);
        }

        // Safely set created and updated dates
        createdAtLabel.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "N/A");
        updatedAtLabel.setText(post.getUpdatedAt() != null ? post.getUpdatedAt() : "N/A");
    }*/

    public void initPostData(Post post) {
        // Safely set the content
        contentLabel.setText(post.getContent() != null ? post.getContent() : "No content available");

        // Safely set the image as a hyperlink
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            imageLink.setText(post.getImage());
            imageLink.setOnAction(event -> openImageInBrowser(post.getImage()));
        } else {
            imageLink.setText("No image available");
            imageLink.setDisable(true);
        }

        // Safely set created and updated dates
        createdAtLabel.setText(post.getCreatedAt() != null ? post.getCreatedAt() : "N/A");
        updatedAtLabel.setText(post.getUpdatedAt() != null ? post.getUpdatedAt() : "N/A");
    }


    /**
     * Handles the close button action.
     * @param actionEvent The action event triggered by the close button.
     */
    @FXML
    private void handleCloseButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Opens the image link in the system's default browser.
     * @param imageUrl The URL of the image to open.
     */
    private void openImageInBrowser(String imageUrl) {
        try {
            Desktop.getDesktop().browse(new URI(imageUrl));
        } catch (Exception e) {
            System.err.println("Failed to open image URL: " + e.getMessage());
        }
    }

    /**
     * Displays the detail view for a specific post.
     * @param post The post to display.
     */
   /* public static void showDetail(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(DetailPostController.class.getResource("/DetailPost.fxml"));
            Parent root = loader.load();

            // Pass the post data to the controller
           DetailPostController controller = loader.getController();
            controller.initPostData(post);

            // Show the detail view in a new stage
            Stage stage = new Stage();
            stage.setTitle("Détails du Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load DetailPost view: " + e.getMessage());
        }
    }*/


    public static void showDetail(Post post) {
        try {
            // Load the DetailPost.fxml file
            FXMLLoader loader = new FXMLLoader(DetailPostController.class.getResource("/DetailPost.fxml"));
            Parent root = loader.load();

            // Pass the post data to the DetailPostController
            DetailPostController controller = loader.getController();
            controller.initPostData(post);

            // Show the detail view in a new stage
            Stage stage = new Stage();
            stage.setTitle("Détails du Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load DetailPost.fxml: " + e.getMessage());
        }
    }


}

