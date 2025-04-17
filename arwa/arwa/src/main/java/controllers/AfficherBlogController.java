package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Blog;
import service.BlogService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherBlogController {

    @FXML private GridPane blogGrid;
    @FXML private Button addButton;
    @FXML private Button btnLogout;

    private final BlogService blogService = new BlogService();

    @FXML
    public void initialize() {
        loadBlogs();
    }

    private void loadBlogs() {
        blogGrid.getChildren().clear();
        try {
            List<Blog> blogs = blogService.select();
            int column = 0;
            int row = 0;

            for (Blog blog : blogs) {
                VBox blogCard = createBlogCard(blog);
                blogGrid.add(blogCard, column, row);

                column++;
                if (column > 2) {
                    column = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load blogs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");
        card.setPrefSize(250, 200);

        Text titleText = new Text(blog.getTitle());
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Text descriptionText = new Text(blog.getDescription());
        Text createdAtText = new Text("Created At: " + blog.getCreatedAtBlog());
        Text updatedAtText = new Text("Updated At: " + blog.getUpdatedAtBlog());

        // Action Buttons
        HBox buttonBox = new HBox(10);

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #397163; -fx-text-fill: white;");
        detailsBtn.setOnAction(e -> showBlogDetails(blog));

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        editBtn.setOnAction(e -> editBlog(blog));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteBlog(blog));

        buttonBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
        card.getChildren().addAll(titleText, descriptionText, createdAtText, updatedAtText, buttonBox);
        return card;
    }

    private void showBlogDetails(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailBlog.fxml"));
            Parent root = loader.load();

            DetailBlogController controller = loader.getController();
            controller.initBlogData(blog);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Blog");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load details view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editBlog(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierBlog.fxml"));
            Parent root = loader.load();

            ModifierBlogController controller = loader.getController();
            controller.initBlogData(blog);
            controller.setRefreshCallback(new Runnable() {
                @Override
                public void run() {
                    AfficherBlogController.this.loadBlogs();
                }
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Blog");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load edit form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteBlog(Blog blog) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le blog");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le blog " + blog.getTitle() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                blogService.delete(blog.getId());
                loadBlogs(); // Refresh the blog list
                showAlert("Succès", "Blog supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterBlog.fxml"));
            Parent root = loader.load();

            AjouterBlogController controller = loader.getController();
            controller.setRefreshCallback(this::loadBlogs);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Blog");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load add form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Logout failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}