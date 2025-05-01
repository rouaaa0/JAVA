package com.example.pi;

import com.dlsc.gmapsfx.javascript.object.MapShape;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EvenementController implements Initializable {

    @FXML
    private TableView<Evenement> tEvenement;

    @FXML
    private TableColumn<Evenement, String> titreColumn;

    @FXML
    private TableColumn<Evenement, String> descriptionColumn;

    @FXML
    private TableColumn<Evenement, Double> prixColumn;

    @FXML
    private TableColumn<Evenement, LocalDate> dateDebutColumn;

    @FXML
    private TableColumn<Evenement, LocalDate> dateFinColumn;

    @FXML
    private TextField tTitre;

    @FXML
    private TextField tDescription;

    @FXML
    private TextField tPrix;

    @FXML
    private DatePicker tDateDebut;

    @FXML
    private DatePicker tDateFin;
    @FXML
    private AnchorPane mapContainer;
    @FXML
    private PieChart eventPieChart;
    private File selectedFile;
    private Connection connection;
    @FXML
    private ListView<Evenement> eventListView;
    private static final int ITEMS_PER_PAGE = 3;
    private int currentPage = 0;
    private List<Evenement> allEvents = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       MapView mapView = new MapView();
        MemoryStorageService memoryStorageService = new MemoryStorageService();

        MapPoint tunis = new MapPoint(36.8065, 10.1815);
        mapView.setZoom(13);
        mapView.flyTo(0., tunis, 1.0);

        // Ajouter la carte dans le conteneur
        mapContainer.getChildren().add(mapView);
        AnchorPane.setTopAnchor(mapView, 0.0);
        AnchorPane.setBottomAnchor(mapView, 0.0);
        AnchorPane.setLeftAnchor(mapView, 0.0);
        AnchorPane.setRightAnchor(mapView, 0.0);
        connection = DBConnexion.getCon();
        setupEventListView();
        refreshTable();

        eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tTitre.setText(newSelection.getTitre());
                tDescription.setText(newSelection.getDescription());
                tPrix.setText(String.valueOf(newSelection.getPrix()));
                tDateDebut.setValue(newSelection.getDateDebut());
                tDateFin.setValue(newSelection.getDateFin());
            } else {
                clear();
            }
        });
    }
    private void updateEventStatistics() {
        int total = 0;
        int cheap = 0, moderate = 0, expensive = 0;

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT prix FROM evenement");

            while (rs.next()) {
                total++;
                double prix = rs.getDouble("prix");
                if (prix < 20) cheap++;
                else if (prix < 50) moderate++;
                else expensive++;
            }

            ObservableList<PieChart.Data> pieChartData =
                    FXCollections.observableArrayList(
                            new PieChart.Data("Économique (< 20 DT)", cheap),
                            new PieChart.Data("Moyen (20-49 DT)", moderate),
                            new PieChart.Data("Cher (>= 50 DT)", expensive)
                    );

            eventPieChart.setData(pieChartData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupEventListView() {
        eventListView.setCellFactory(param -> new ListCell<Evenement>() {
            @Override
            protected void updateItem(Evenement evenement, boolean empty) {
                super.updateItem(evenement, empty);
                if (empty || evenement == null) {
                    setText(null);
                } else {
                    setText("📅 " + evenement.getTitre() + "\n"
                            + "📝 " + evenement.getDescription() + "\n"
                            + "💵 " + evenement.getPrix() + " DT\n"
                            + "Du " + evenement.getDateDebut() + " au " + evenement.getDateFin());
                    setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
                }
            }
        });
    }

    private void refreshTable() {
        allEvents.clear();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM evenement");
            while (rs.next()) {
                Evenement evenement = new Evenement(
                        rs.getInt("id"),
                        rs.getInt("club_id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getDouble("x"),
                        rs.getDouble("y")
                );
                allEvents.add(evenement);
            }
            updateEventStatistics();
            updateListView(); // Affiche la page actuelle

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateListView() {
        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allEvents.size());
        if (fromIndex <= toIndex) {
            eventListView.setItems(FXCollections.observableArrayList(allEvents.subList(fromIndex, toIndex)));
        }
    }
    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateListView();
        }
    }

    @FXML
    private void nextPage() {
        if ((currentPage + 1) * ITEMS_PER_PAGE < allEvents.size()) {
            currentPage++;
            updateListView();
        }
    }

    @FXML
    private void clear() {
        tTitre.clear();
        tDescription.clear();
        tPrix.clear();
        tDateDebut.setValue(null);
        tDateFin.setValue(null);
    }

    @FXML
    private void createEvenement() {
        String titre = tTitre.getText().trim();
        String description = tDescription.getText().trim();
        String prixText = tPrix.getText().trim();
        LocalDate dateDebut = tDateDebut.getValue();
        LocalDate dateFin = tDateFin.getValue();

        if (titre.isEmpty() || description.isEmpty() || prixText.isEmpty() || dateDebut == null || dateFin == null) {
            showNotification("Veuillez remplir tous les champs.");
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixText);
        } catch (NumberFormatException e) {
            showNotification("Le prix doit être un nombre valide.");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO evenement(titre, description, prix, date_debut, date_fin, x, y) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, titre);
            ps.setString(2, description);
            ps.setDouble(3, prix);
            ps.setDate(4, Date.valueOf(dateDebut));
            ps.setDate(5, Date.valueOf(dateFin));
            ps.setDouble(6, 0.0); // x
            ps.setDouble(7, 0.0); // y
            ps.executeUpdate();
            refreshTable();

            // Envoi d'un email après l'ajout de l'événement
            sendEmail("destinataire@example.com", "Nouvel événement créé",
                    "Un nouvel événement a été créé : \n\nTitre : " + titre + "\nDescription : " + description +
                            "\nPrix : " + prix + "\nDate début : " + dateDebut + "\nDate fin : " + dateFin);
            showNotification("L'événement a été ajouté avec succès et un e-mail a été envoyé!");

        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        String from = "gharbi.wided@esprit.tn"; // L'email de l'expéditeur
        String host = "smtp.gmail.com"; // Serveur SMTP (par exemple, smtp.gmail.com pour Gmail)
        final String username = "gharbi.wided@esprit.tn"; // Votre adresse email
        final String password = "vnty xvfe bcmb enpu"; // Votre mot de passe email

        // Configuration des propriétés de l'email
        Email email = new HtmlEmail();
        try {
            email.setHostName(host);
            email.setSmtpPort(587); // Port pour le serveur SMTP
            email.setAuthentication(username, password);
            email.setSSLOnConnect(true);
            email.setFrom(from);
            email.addTo(to);
            email.setSubject(subject);
            email.setMsg(body);

            // Envoi de l'email
            email.send();
            System.out.println("Email envoyé avec succès.");

        } catch (EmailException e) {
            e.printStackTrace();
            showNotification("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    @FXML
    private void updateEvenement() {
        Evenement selected = eventListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Veuillez sélectionner un événement.");
            return;
        }

        String titre = tTitre.getText().trim();
        String description = tDescription.getText().trim();
        String prixText = tPrix.getText().trim();
        LocalDate dateDebut = tDateDebut.getValue();
        LocalDate dateFin = tDateFin.getValue();

        if (titre.isEmpty() || description.isEmpty() || prixText.isEmpty() || dateDebut == null || dateFin == null) {
            showNotification("Veuillez remplir tous les champs.");
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixText);
        } catch (NumberFormatException e) {
            showNotification("Le prix doit être un nombre valide.");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE evenement SET titre = ?, description = ?, prix = ?, date_debut = ?, date_fin = ? WHERE id = ?");
            ps.setString(1, titre);
            ps.setString(2, description);
            ps.setDouble(3, prix);
            ps.setDate(4, Date.valueOf(dateDebut));
            ps.setDate(5, Date.valueOf(dateFin));
            ps.setInt(6, selected.getId());
            ps.executeUpdate();
            refreshTable();
            showNotification("Événement mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void deleteEvenement() {
        Evenement selected = eventListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Veuillez sélectionner un événement.");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM evenement WHERE id = ?");
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            refreshTable();
            showNotification("Événement supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        }
    }

    private void showNotification(String message) {
        Notifications.create()
                .title("Notification")
                .text(message)
                .position(Pos.CENTER)
                .owner(eventListView.getScene().getWindow())
                .showInformation();
    }

    @FXML
    private void goToMyDrivePage(ActionEvent event) {
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
    private void goToMyDrivePageC(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Club.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToMyDrivePageM(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Calendar.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
