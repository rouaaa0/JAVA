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
import models.User;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherUserController {

    @FXML private GridPane userGrid;
    @FXML private Button addButton;
    @FXML private Button btnLogout;
    @FXML private Button modifierPasswordButton;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        userGrid.getChildren().clear();
        try {
            List<User> users = userService.select();
            int column = 0;
            int row = 0;

            for (User user : users) {
                VBox userCard = createUserCard(user);
                userGrid.add(userCard, column, row);

                column++;
                if (column > 2) {
                    column = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");
        card.setPrefSize(250, 200);

        Text nameText = new Text(user.getName() + " " + user.getLastname());
        nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Text emailText = new Text(user.getEmail());
        Text roleText = new Text("Role: " + user.getRole());

        // Action Buttons
        HBox buttonBox = new HBox(10);

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #397163; -fx-text-fill: white;");
        detailsBtn.setOnAction(e -> showUserDetails(user));

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
        editBtn.setOnAction(e -> editUser(user));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteUser(user));

        buttonBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
        card.getChildren().addAll(nameText, emailText, roleText, buttonBox);
        return card;
    }

    private void showUserDetails(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailUser.fxml"));
            Parent root = loader.load();

            DetailUserController controller = loader.getController();
            controller.initUserData(user);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails Utilisateur");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load details view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            ModifierUserController controller = loader.getController();
            controller.initUserData(user);
            controller.setRefreshCallback(t -> loadUsers());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Utilisateur");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load edit form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getName() + " " + user.getLastname() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.delete(user.getId());
                loadUsers(); // Refresh the user list
                showAlert("Succès", "Utilisateur supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();

            AjouterUserController controller = loader.getController();
            controller.setRefreshCallback(this::loadUsers);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Utilisateur");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load add form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUsers(Void unused) {

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

    @FXML
    private void handleModifyPassword(ActionEvent event) {
        // Implement password modification logic
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}