package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Post;
import service.PostService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherPostController {

    @FXML
    private GridPane postGrid;

    @FXML
    private ScrollPane scrollPane;

    /**
     * Initializes the controller and loads the posts.
     */
    @FXML
    private void initialize() {
        refreshPostList();
    }

    /**
     * Refreshes the post list by fetching data from the database.
     */
    private void refreshPostList() {
        PostService postService = new PostService();
        try {
            List<Post> posts = postService.select();
            if (posts == null || posts.isEmpty()) {
                showAlert("Information", "Aucun post à afficher.", Alert.AlertType.INFORMATION);
                postGrid.getChildren().clear();
                return;
            }
            populatePostGrid(posts);
        } catch (SQLException e) {
            logError(e);
            showAlert("Erreur", "Une erreur s'est produite lors du chargement des posts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Populates the grid with post cards.
     *
     * @param posts The list of posts to display.
     */
    private void populatePostGrid(List<Post> posts) {
        postGrid.getChildren().clear(); // Clear any existing elements

        int column = 0;
        int row = 0;

        for (Post post : posts) {
            VBox postCard = new VBox(10);
            postCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 15; -fx-background-radius: 10;");
            postCard.setPrefWidth(250);
            postCard.setPrefHeight(200);

            // Post Title
            Text title = new Text(post.getContent().length() > 20 ? post.getContent().substring(0, 20) + "..." : post.getContent());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            postCard.getChildren().add(title);

            // Post Content
            Text content = new Text(post.getContent());
            content.setStyle("-fx-font-size: 14px;");
            postCard.getChildren().add(content);

            // Post Created Timestamp
            Text createdAt = new Text("Créé le: " + post.getCreatedAt());
            createdAt.setStyle("-fx-font-size: 12px; -fx-fill: gray;");
            postCard.getChildren().add(createdAt);

            // Action Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setStyle("-fx-padding: 5;");

            Button detailsButton = new Button("Détails");
            detailsButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");
            detailsButton.setOnAction(event -> handleDetailsAction(post));

            Button modifyButton = new Button("Modifier");
            modifyButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");
            modifyButton.setOnAction(event -> handleModifyAction(post));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5;");
            deleteButton.setOnAction(event -> handleDeleteAction(post));

            buttonBox.getChildren().addAll(detailsButton, modifyButton, deleteButton);
            postCard.getChildren().add(buttonBox);

            // Add the card to the grid
            postGrid.add(postCard, column, row);

            column++;
            if (column == 3 ) { // 3 cards per row
                column = 0;
                row++;
            }
        }
    }

    /**
     * Handles the "Add Post" button action.
     */
    @FXML
    private void handleAddPostAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPost.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh posts after the window closes
            refreshPostList();
        } catch (IOException e) {
            logError(e);
            showAlert("Erreur", "Une erreur s'est produite lors de l'ouverture de la fenêtre: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the "Details" button action for a post.
     *
     * @param post The post to display details for.
     */
    private void handleDetailsAction(Post post) {
        showAlert("Détails", "Contenu: " + post.getContent() + "\nImage: " + (post.getImage() != null ? post.getImage() : "Aucune image") + "\nCréé le: " + post.getCreatedAt(), Alert.AlertType.INFORMATION);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailPost.fxml"));
        try {
            Parent root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Handles the "Modify" button action for a post.
     *
     * @param post The post to modify.
     */
    private void handleModifyAction(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPost.fxml"));
            Parent root = loader.load();

            // Pass the post to the ModifierPostController
            ModifierPostController controller = loader.getController();
            controller.initPostData(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier un Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh posts after modification
            refreshPostList();
        } catch (IOException e) {
            logError(e);
            showAlert("Erreur", "Une erreur s'est produite lors de l'ouverture de la fenêtre de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the "Delete" button action for a post.
     *
     * @param post The post to delete.
     */
   /* private void handleDeleteAction(Post post) {
        PostService postService = new PostService();
        try {
            postService.delete(post.getId());
            showAlert("Succès", "Le post a été supprimé avec succès.", Alert.AlertType.INFORMATION);

            // Refresh posts after deletion
            refreshPostList();
        } catch (SQLException e) {
            logError(e);
            showAlert("Erreur", "Une erreur s'est produite lors de la suppression du post: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }*/

    private void handleDeleteAction(Post post) {
        // Display a confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce post ?");
        confirmationAlert.setContentText("Cette action est irréversible.");

        // Add "Yes" and "No" options
        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirmationAlert.getButtonTypes().setAll(yesButton, noButton);

        // Wait for the user's response
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // User confirmed deletion
            PostService postService = new PostService();
            try {
                postService.delete(post.getId());
                showAlert("Succès", "Le post a été supprimé avec succès.", Alert.AlertType.INFORMATION);

                // Refresh posts after deletion
                refreshPostList();
            } catch (SQLException e) {
                logError(e);
                showAlert("Erreur", "Une erreur s'est produite lors de la suppression du post: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            // User canceled deletion
            showAlert("Annulé", "La suppression a été annulée.", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Displays an alert dialog.
     *
     * @param title   The title of the alert.
     * @param message The content of the alert.
     * @param type    The type of the alert.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Logs an error to the console.
     *
     * @param e The exception to log.
     */
    private void logError(Exception e) {
        System.err.println("An error occurred: " + e.getMessage());
        e.printStackTrace();
    }
}