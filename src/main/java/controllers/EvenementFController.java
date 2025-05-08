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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import javafx.stage.StageStyle;
import models.Evenement;
import utils.SessionManager;

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

public class EvenementFController implements Initializable {

    @FXML
    private HBox recenthb;
    @FXML
    private VBox tosevents;

    private Connection connection;
    @FXML
    private javafx.scene.control.TextField searchField;
    @FXML
    private ComboBox<String> sortCriteriaComboBox;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    @FXML
    private void onSortChanged() {
        updateEventDisplayWithSort();
    }
    private boolean darkMode = false;

    @FXML
    private Button themeToggleBtn;
    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connection = DBConnexion.getCon();
        List<Evenement> sorted = getEvenementsFromDB();

        int limit = Math.min(3, sorted.size());

        for (int i = 0; i < limit; i++) {
            Evenement e = sorted.get(i);
            recenthb.getChildren().add(createEventCard(e));
        }

        for (int i = 3; i < sorted.size(); i++) {
            Evenement e = sorted.get(i);
            tosevents.getChildren().add(createEventHBox(e));
        }

        sortCriteriaComboBox.setItems(FXCollections.observableArrayList("Titre", "Prix", "Date de début"));
        sortOrderComboBox.setItems(FXCollections.observableArrayList("Ascendant", "Descendant"));

        sortCriteriaComboBox.getSelectionModel().select("Titre");
        sortOrderComboBox.getSelectionModel().select("Ascendant");

        updateEventDisplayWithSort();
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


    private List<Evenement> getEvenementsFromDB() {
        ObservableList<Evenement> evenements = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM evenement ORDER BY id DESC");
            while (rs.next()) {
                Evenement evenement = new Evenement(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate()
                );
                evenements.add(evenement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de récupération des événements");
        }

        return evenements.stream()
                .sorted(Comparator.comparingInt(Evenement::getId).reversed())
                .collect(Collectors.toList());
    }

    private AnchorPane createEventCard(Evenement e) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(191.0, 140.0);
        card.getStyleClass().add("event-card");
        card.setEffect(new DropShadow());

        Image image;
        try {
            image = new Image(getClass().getResourceAsStream("/images/ev.png"));
        } catch (Exception ex) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Image par défaut introuvable.");
            image = new Image("https://via.placeholder.com/50");
        }

        ImageView icon = new ImageView(image);
        icon.setFitHeight(44.0);
        icon.setFitWidth(51.0);
        icon.setLayoutX(24.0);
        icon.setLayoutY(10.0);
        icon.setPreserveRatio(true);

        Label titleLabel = new Label(e.getTitre());
        titleLabel.setFont(Font.font("Berlin Sans FB", 14.0));
        titleLabel.setTextFill(Color.web("#2262c6"));
        titleLabel.setLayoutX(10.0);
        titleLabel.setLayoutY(65.0);

        Label descLabel = new Label(e.getDescription());
        descLabel.setFont(Font.font("Arial", 12.0));
        descLabel.setTextFill(Color.web("#555555"));
        descLabel.setLayoutX(10.0);
        descLabel.setLayoutY(85.0);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(170.0);

        Label priceLabel = new Label("Prix: " + e.getPrix() + "DT");
        priceLabel.setFont(Font.font("Arial", 12.0));
        priceLabel.setTextFill(Color.web("#777777"));
        priceLabel.setLayoutX(10.0);
        priceLabel.setLayoutY(110.0);

        card.getChildren().addAll(icon, titleLabel, descLabel, priceLabel);

        card.setOnMouseEntered(e1 -> card.setStyle("-fx-background-color: #f0f8ff;"));
        card.setOnMouseExited(e1 -> card.setStyle("-fx-background-color: transparent;"));

        return card;
    }

    private HBox createEventHBox(Evenement event) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(15.0);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        ImageView icon;
        try {
            icon = new ImageView(new Image(getClass().getResourceAsStream("/images/ev.png")));
        } catch (Exception e) {
            icon = new ImageView();
        }
        icon.setFitWidth(35.0);
        icon.setFitHeight(30.0);

        Label titleLabel = new Label("Titre : " + event.getTitre());
        titleLabel.setTextFill(Color.web("#2262c6"));
        titleLabel.setFont(Font.font("Berlin Sans FB", 14.0));

        Label descriptionLabel = new Label("Description : " + event.getDescription());
        descriptionLabel.setTextFill(Color.web("#555555"));
        descriptionLabel.setFont(Font.font("Arial", 13.0));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);

        Label priceLabel = new Label("Prix : " + event.getPrix() + " DT");
        priceLabel.setTextFill(Color.web("#777777"));
        priceLabel.setFont(Font.font("Arial", 12.0));

        Label dateDebutLabel = new Label("Début : " + event.getDateDebut().toString());
        dateDebutLabel.setTextFill(Color.web("#777777"));
        dateDebutLabel.setFont(Font.font("Arial", 12.0));

        Label dateFinLabel = new Label("Fin : " + event.getDateFin().toString());
        dateFinLabel.setTextFill(Color.web("#777777"));
        dateFinLabel.setFont(Font.font("Arial", 12.0));

        VBox textBox = new VBox(titleLabel, descriptionLabel, priceLabel, dateDebutLabel, dateFinLabel);
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
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/EvenementF.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement EvenementF.fxml : " + e.getMessage());
        }
    }

    @FXML
    private void goToMyDrivePageC(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/ClubF.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement ClubF.fxml : " + e.getMessage());
        }
    }
    @FXML
    private void onSearchKeyReleased() {
        String searchText = searchField.getText().toLowerCase().trim();

        // Si le champ est vide, afficher tous les événements
        if (searchText.isEmpty()) {
            updateEventDisplay(getEvenementsFromDB());
            return;
        }

        // Filtrer les événements
        List<Evenement> filteredEvents = getEvenementsFromDB().stream()
                .filter(ev -> ev.getTitre().toLowerCase().contains(searchText) ||
                        ev.getDescription().toLowerCase().contains(searchText) ||
                        String.valueOf(ev.getPrix()).contains(searchText))
                .collect(Collectors.toList());

        // Mise à jour de l'affichage
        updateEventDisplay(filteredEvents);
    }

    @FXML
    private void updateEventDisplayWithSort() {
        String critere = sortCriteriaComboBox.getValue();
        String ordre = sortOrderComboBox.getValue();

        List<Evenement> events = getEvenementsFromDB();

        if (critere != null && ordre != null) {
            Comparator<Evenement> comparator;

            switch (critere) {
                case "Titre":
                    comparator = Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER);
                    break;
                case "Prix":
                    comparator = Comparator.comparing(Evenement::getPrix);
                    break;
                case "Date de début":
                    comparator = Comparator.comparing(Evenement::getDateDebut);
                    break;
                default:
                    comparator = Comparator.comparingInt(Evenement::getId).reversed(); // fallback
            }

            if ("Descendant".equals(ordre)) {
                comparator = comparator.reversed();
            }

            events = events.stream().sorted(comparator).collect(Collectors.toList());
        }

        updateEventDisplay(events);
    }
    private void updateEventDisplay(List<Evenement> events) {
        tosevents.getChildren().clear();

        for (Evenement ev : events) {
            tosevents.getChildren().add(createEventHBox(ev));
        }
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
}
