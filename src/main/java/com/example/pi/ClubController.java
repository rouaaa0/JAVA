package com.example.pi;

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
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import org.controlsfx.control.Notifications;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;

public class ClubController implements Initializable {

    @FXML
    private TableView<Club> tClub;

    @FXML
    private TableColumn<Club, String> nomColumn;

    @FXML
    private TableColumn<Club, String> typeColumn;

    @FXML
    private TableColumn<Club, String> logoColumn;

    @FXML
    private TableColumn<Club, LocalDate> dateColumn;

    @FXML
    private TextField tNom;

    @FXML
    private TextField tType;

    @FXML
    private DatePicker tDateCreation;

    @FXML
    private ImageView imageView;

    @FXML
    private Label errorNom, errorType, errorDate, errorLogo;
    @FXML
    private PieChart pieChart;  // Pour la répartition des clubs par type
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private File selectedLogoFile;
    private Connection connection;
    @FXML
    private ListView<Club> clubListView;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBConnexion.getCon();
        refreshListView();
        setupClubListView();
        initPieChart();
        clubListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            if (newSelection != null) {
                tNom.setText(newSelection.getNom());
                tType.setText(newSelection.getType());
                tDateCreation.setValue(newSelection.getDateCreation());
                String logoPath = newSelection.getLogo();
                if (logoPath != null && !logoPath.isEmpty()) {
                    imageView.setImage(new Image(new File(logoPath).toURI().toString()));
                } else {
                    imageView.setImage(null);
                }
            } else {
                clear();
            }
        });
    }
    private void initPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT type, COUNT(*) AS count FROM club GROUP BY type");
            while (rs.next()) {
                String type = rs.getString("type");
                int count = rs.getInt("count");
                pieChartData.add(new PieChart.Data(type + " (" + count + ")", count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pieChart.setData(pieChartData);
    }

    private void updateStatistics() {
        int totalClubs = 0;
        int clubWithLogo = 0;
        int clubWithoutLogo = 0;
        Map<String, Integer> typeDistribution = new HashMap<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM club");

            while (rs.next()) {
                totalClubs++;
                String logoPath = rs.getString("logo");
                if (logoPath != null && !logoPath.isEmpty()) {
                    clubWithLogo++;
                } else {
                    clubWithoutLogo++;
                }

                String type = rs.getString("type");
                typeDistribution.put(type, typeDistribution.getOrDefault(type, 0) + 1);
            }


            // Répartition des types de clubs
            StringBuilder typeStats = new StringBuilder("Répartition des Types:\n");
            for (Map.Entry<String, Integer> entry : typeDistribution.entrySet()) {
                typeStats.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupClubListView() {
        clubListView.setCellFactory(param -> new ListCell<Club>() {
            @Override
            protected void updateItem(Club club, boolean empty) {
                super.updateItem(club, empty);
                if (empty || club == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Format des infos
                    String text = "🏷️ Nom : " + club.getNom() + "\n" +
                            "📂 Type : " + club.getType() + "\n" +
                            "📅 Créé le : " + club.getDateCreation() + "\n" +
                            "🆔 ID : " + club.getId() + " | Utilisateur : " + club.getUserId();

                    setText(text);
                    setStyle("-fx-padding: 10px; -fx-font-size: 14px;");

                    // Pour afficher le logo
                    if (club.getLogo() != null && !club.getLogo().isEmpty()) {
                        try {
                            Image image = new Image("file:" + club.getLogo(), 60, 60, true, true);
                            ImageView imageView = new ImageView(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            System.out.println("Erreur lors du chargement de l'image : " + e.getMessage());
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }


    private void refreshListView() {
        ObservableList<Club> clubs = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM club");
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
            clubListView.setItems(clubs);
            updateStatistics();
            clubListView.setCellFactory(param -> new ListCell<Club>() {
                @Override
                protected void updateItem(Club item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " | " + item.getType() + " | " + item.getDateCreation());
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTableView() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        logoColumn.setCellValueFactory(new PropertyValueFactory<>("logo"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
    }

    private void refreshTable() {
        ObservableList<Club> clubs = FXCollections.observableArrayList();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM club");
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
            clubListView.setItems(clubs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void uploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedLogoFile = fileChooser.showOpenDialog(null);
        if (selectedLogoFile != null) {
            imageView.setImage(new Image(selectedLogoFile.toURI().toString()));
        }
    }

    @FXML
    private void clear() {
        tNom.clear();
        tType.clear();
        tDateCreation.setValue(null);
        imageView.setImage(null);
        selectedLogoFile = null;

        // Effacer les messages d'erreur
        errorNom.setText("");
        errorType.setText("");
        errorDate.setText("");
        errorLogo.setText("");
    }

    private boolean validateInputs(String nom, String type, LocalDate date, String logoPath, boolean checkLogoRequired) {
        boolean isValid = true;

        // Effacer les anciens messages d'erreur
        errorNom.setText("");
        errorType.setText("");
        errorDate.setText("");
        errorLogo.setText("");

        // Validation des champs
        if (nom.isEmpty()) {
            errorNom.setText("Le nom est obligatoire.");
            isValid = false;
        } else if (!nom.matches("[a-zA-Z\\s]+")) {
            errorNom.setText("Le nom ne doit contenir que des lettres.");
            isValid = false;
        }

        if (type.isEmpty()) {
            errorType.setText("Le type est obligatoire.");
            isValid = false;
        } else if (!type.matches("[a-zA-Z\\s]+")) {
            errorType.setText("Le type ne doit contenir que des lettres.");
            isValid = false;
        }

        if (date == null) {
            errorDate.setText("La date de création est obligatoire.");
            isValid = false;
        }

        if (checkLogoRequired && logoPath.isEmpty()) {
            errorLogo.setText("Veuillez uploader un logo.");
            isValid = false;
        } else if (!logoPath.isEmpty() && !(logoPath.endsWith(".png") || logoPath.endsWith(".jpg") || logoPath.endsWith(".jpeg"))) {
            errorLogo.setText("Le fichier logo doit être une image (PNG, JPG, JPEG).");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void createClub() {
        String nom = tNom.getText().trim();
        String type = tType.getText().trim();
        LocalDate dateCreation = tDateCreation.getValue();
        String logoPath = selectedLogoFile != null ? selectedLogoFile.getAbsolutePath() : "";

        if (!validateInputs(nom, type, dateCreation, logoPath, true)) return;

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO club(nom, type, logo, date_creation, user_id) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, nom);
            ps.setString(2, type);
            ps.setString(3, logoPath);
            ps.setDate(4, Date.valueOf(dateCreation));
            ps.setInt(5, 1); // À adapter selon l'utilisateur connecté
            ps.executeUpdate();
            refreshTable();
            clear();
            showNotification("Club ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void updateClub() {
        Club selected = clubListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showNotification("Veuillez sélectionner un club.");
            return;
        }

        String nom = tNom.getText().trim();
        String type = tType.getText().trim();
        LocalDate dateCreation = tDateCreation.getValue();
        String logoPath = selectedLogoFile != null ? selectedLogoFile.getAbsolutePath() : selected.getLogo();

        if (!validateInputs(nom, type, dateCreation, logoPath, false)) return;

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE club SET nom = ?, type = ?, logo = ?, date_creation = ? WHERE id = ?");
            ps.setString(1, nom);
            ps.setString(2, type);
            ps.setString(3, logoPath);
            ps.setDate(4, Date.valueOf(dateCreation));
            ps.setInt(5, selected.getId());
            ps.executeUpdate();
            refreshTable();
            clear();
            showNotification("Club mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void deleteClub() {
        Club selected = clubListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Veuillez sélectionner un club.");
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM club WHERE id = ?");
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            refreshTable();
            clear();
            showNotification("Club supprimé !");
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
                .owner(clubListView.getScene().getWindow())
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

    @FXML
    private void exportToPDF() {
        // Récupérer le club sélectionné depuis la liste
        Club selected = clubListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showNotification("Veuillez sélectionner un club.");
            return;
        }

        try {
            // Création de la requête SQL pour récupérer les détails du club
            PreparedStatement ps = connection.prepareStatement("SELECT nom, type, date_creation FROM club WHERE id = ?");
            ps.setInt(1, selected.getId()); // Utiliser l'ID du club sélectionné
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String type = rs.getString("type");
                String date = rs.getDate("date_creation") != null
                        ? rs.getDate("date_creation").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "Non précisée";

                // Création du fichier PDF
                FileOutputStream fileOut = new FileOutputStream("ClubDetails_" + selected.getId() + ".pdf");

                // Création du writer pour le fichier PDF
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                PdfWriter writer = PdfWriter.getInstance(document, fileOut);

                // Ouvrir le document pour y ajouter du contenu
                document.open(); // Ouvrir le document avant d'ajouter du contenu

                // Titre du document
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
                Paragraph title = new Paragraph("Détails du Club", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);  // Appliquer l'alignement
                title.setSpacingBefore(20);  // Ajouter de l'espace avant le titre
                title.setSpacingAfter(10);   // Ajouter de l'espace après le titre
                document.add(title);

                // Créer un tableau avec 2 colonnes
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);

                // Ajouter des entêtes de table
                table.addCell("Champ");
                table.addCell("Valeur");

                // Ajouter des données dans le tableau
                table.addCell("Nom");
                table.addCell(nom);

                table.addCell("Type");
                table.addCell(type);

                table.addCell("Date de création");
                table.addCell(date);

                // Ajouter le tableau au document
                document.add(table);

                // Fermer le document
                document.close(); // Fermer le document
                writer.close();   // Fermer le writer

                System.out.println("PDF généré avec succès !");
                showNotification("PDF généré avec succès !");
            } else {
                showNotification("Aucun club trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

}
