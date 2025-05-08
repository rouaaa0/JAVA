package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.stage.StageStyle;
import models.Club;
import utils.SessionManager;

public class ClubFController implements Initializable {

    @FXML
    private HBox recenthb;
    @FXML
    private VBox toclubs;

    @FXML
    private void onSortChanged() {
        updateClubDisplayWithSort();
    }

    private Connection connection;
    @FXML
    private javafx.scene.control.TextField searchField;
    @FXML
    private ComboBox<String> sortCriteriaComboBox;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    private boolean darkMode = false;
    private boolean isDarkMode = false;

    @FXML
    private Button themeToggleBtn;
    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = DBConnexion.getCon();
        List<Club> sorted = getClubsFromDB();

        int limit = Math.min(3, sorted.size());

        // Ajouter les 3 premiers clubs à la HBox
        for (int i = 0; i < limit; i++) {
            Club c = sorted.get(i);
            recenthb.getChildren().add(createClubCard(c));
        }

        // Ajouter les autres clubs à la VBox (défilement)
        for (int i = 3; i < sorted.size(); i++) {
            Club c = sorted.get(i);
            toclubs.getChildren().add(createClubHBox(c));
        }

        sortCriteriaComboBox.setItems(FXCollections.observableArrayList("Nom", "Date"));
        sortOrderComboBox.setItems(FXCollections.observableArrayList("Ascendant", "Descendant"));

        sortCriteriaComboBox.getSelectionModel().select("Nom");
        sortOrderComboBox.getSelectionModel().select("Ascendant");

        updateClubDisplayWithSort();
    }

    public void toggleTheme(javafx.event.ActionEvent actionEvent) {
        Scene scene = ((Node) actionEvent.getSource()).getScene();  // Récupère la scène à partir de l'événement

        // Vérifie quel thème est appliqué et bascule entre le thème clair et sombre
        if (scene.getStylesheets().contains("/css/dark-theme.css")) {
            scene.getStylesheets().remove("/css/dark-theme.css");
            scene.getStylesheets().add("/css/stylesheet1.css");  // Thème clair
        } else {
            scene.getStylesheets().remove("/css/stylesheet1.css");
            scene.getStylesheets().add("/css/dark-theme.css");  // Thème sombre
        }
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profileDialog.fxml"));
            Parent root = loader.load();
            Stage profileStage = new Stage();
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initStyle(StageStyle.DECORATED);
            profileStage.setTitle("Modifier le profil");
            profileStage.setScene(new Scene(root));
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setX(primaryStage.getX() + 100);
            profileStage.setY(primaryStage.getY() + 100);
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de profil.");
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter.");
        }
    }


    @FXML
    private void goToAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Accueil.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil.");
        }
    }

    @FXML
    private void goToBlogsPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogF.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de blog.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToPostPage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/PostPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void goToEventPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/EvenementF.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de blog.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void goToClubPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ClubF.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de blog.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<Club> getClubsFromDB() {
        ObservableList<Club> clubs = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM club ORDER BY id DESC");
            while (rs.next()) {
                Club club = new Club(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("logo"),
                        rs.getString("type"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getInt("user_id")
                );
                clubs.add(club);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de récupération des clubs");
        }

        return clubs.stream()
                .sorted(Comparator.comparingInt(Club::getId).reversed())
                .collect(Collectors.toList());
    }

    private AnchorPane createClubCard(Club c) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(191.0, 140.0);
        card.getStyleClass().add("club-card");
        card.setEffect(new DropShadow());

        Image image;
        try {
            String logoPath = c.getLogo();

            // Vérification si le chemin est un chemin absolu ou une ressource dans le projet
            if (logoPath.startsWith("C:\\")) {
                // Si c'est un chemin absolu, charger l'image à partir de l'ordinateur local
                image = new Image("file:///" + logoPath);
            } else {
                // Si c'est une ressource dans le projet, utiliser getResource
                image = new Image(getClass().getResource(logoPath) != null
                        ? getClass().getResource(logoPath).toString()
                        : "https://via.placeholder.com/50");
            }

        } catch (Exception ex) {
            // Si une erreur survient, utiliser l'image par défaut
            image = new Image("https://via.placeholder.com/50");
        }

        ImageView icon = new ImageView(image);
        icon.setFitHeight(44.0);
        icon.setFitWidth(51.0);
        icon.setLayoutX(24.0);
        icon.setLayoutY(10.0);
        icon.setPreserveRatio(true);

        Label titleLabel = new Label(c.getNom());
        titleLabel.setFont(Font.font("Berlin Sans FB", 14.0));
        titleLabel.setTextFill(Color.web("#2262c6"));
        titleLabel.setLayoutX(10.0);
        titleLabel.setLayoutY(65.0);

        Label typeLabel = new Label("Type : " + c.getType());
        typeLabel.setFont(Font.font("Arial", 12.0));
        typeLabel.setTextFill(Color.web("#555555"));
        typeLabel.setLayoutX(10.0);
        typeLabel.setLayoutY(85.0);

        Label dateLabel = new Label("Création : " + c.getDateCreation().toString());
        dateLabel.setFont(Font.font("Arial", 12.0));
        dateLabel.setTextFill(Color.web("#777777"));
        dateLabel.setLayoutX(10.0);
        dateLabel.setLayoutY(110.0);

        card.getChildren().addAll(icon, titleLabel, typeLabel, dateLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0f8ff;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: transparent;"));

        return card;
    }

    private HBox createClubHBox(Club club) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(15.0);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        ImageView icon;
        try {
            // Si le chemin du logo est un chemin local (ex: C:/Users/...), utilisez File pour créer un objet Image
            String logoPath = club.getLogo(); // Exemple : C:\Users\WIDED\OneDrive\Images\Capture d’écran 202...

            // Si le chemin est valide, charger l'image
            icon = new ImageView(new Image("file:///" + logoPath.replace("\\", "/")));

            // Vérification si l'image existe
            if (icon.getImage() == null) {
                // Si l'image est introuvable, utiliser une image par défaut
                icon.setImage(new Image("https://via.placeholder.com/50"));
            }

        } catch (Exception e) {
            // Si une erreur survient, utiliser l'image par défaut
            icon = new ImageView(new Image("https://via.placeholder.com/50"));
        }

        // Définir la taille de l'image
        icon.setFitWidth(35.0);
        icon.setFitHeight(30.0);

        Label nameLabel = new Label("Nom : " + club.getNom());
        nameLabel.setTextFill(Color.web("#2262c6"));
        nameLabel.setFont(Font.font("Berlin Sans FB", 14.0));

        Label typeLabel = new Label("Type : " + club.getType());
        typeLabel.setTextFill(Color.web("#555555"));
        typeLabel.setFont(Font.font("Arial", 13.0));
        typeLabel.setWrapText(true);
        typeLabel.setMaxWidth(200);

        Label dateLabel = new Label("Création : " + club.getDateCreation().toString());
        dateLabel.setTextFill(Color.web("#777777"));
        dateLabel.setFont(Font.font("Arial", 12.0));

        VBox textBox = new VBox(nameLabel, typeLabel, dateLabel);
        textBox.setSpacing(4);

        hbox.getChildren().addAll(icon, textBox);

        hbox.setOnMouseEntered(e -> hbox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #ccc;"));
        hbox.setOnMouseExited(e -> hbox.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd;"));

        return hbox;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }


    @FXML
    private void goToMyDrivePage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Evenement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMyDrivePageC(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Evenement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearchKeyReleased() {
        String searchText = searchField.getText().toLowerCase().trim();

        // Si le champ est vide, afficher tous les clubs
        if (searchText.isEmpty()) {
            updateClubDisplay(getClubsFromDB());
            return;
        }

        // Filtrer les clubs
        List<Club> filteredClubs = getClubsFromDB().stream()
                .filter(club -> club.getNom().toLowerCase().contains(searchText) ||
                        club.getType().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        // Mise à jour de l'affichage
        updateClubDisplay(filteredClubs);
    }

    private void updateClubDisplay(List<Club> clubs) {
        toclubs.getChildren().clear();

        for (Club c : clubs) {
            toclubs.getChildren().add(createClubHBox(c));
        }
    }

    private void updateClubDisplayWithSort() {
        String critere = sortCriteriaComboBox.getValue();
        String ordre = sortOrderComboBox.getValue();

        List<Club> clubs = getClubsFromDB();

        if (critere != null && ordre != null) {
            Comparator<Club> comparator;

            switch (critere) {
                case "Nom":
                    comparator = Comparator.comparing(Club::getNom, String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Date":
                    comparator = Comparator.comparing(Club::getDateCreation);
                    break;
                default:
                    comparator = Comparator.comparingInt(Club::getId).reversed(); // fallback
            }

            if ("Descendant".equals(ordre)) {
                comparator = comparator.reversed();
            }

            clubs = clubs.stream().sorted(comparator).collect(Collectors.toList());
        }

        updateClubDisplay(clubs);
    }


}
