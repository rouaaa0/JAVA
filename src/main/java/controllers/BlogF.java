package controllers;

import javafx.animation.*;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Blog;
import utils.MyDataBase;
import utils.SessionManager;

public class BlogF implements Initializable {

    @FXML
    private HBox recenthb;
    @FXML
    private VBox toblogs;
    @FXML
    private Connection connection;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    @FXML
    private ToggleButton themeToggle;
    @FXML
    private Button chatButton;

    private boolean isDarkMode = false;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean wasDragged = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = MyDataBase.getInstance().getConnection();
        List<Blog> sorted = getBlogsFromDB();

        // Initialize theme toggle button
        if (themeToggle != null) {
            themeToggle.setText("Dark Mode"); // Initial text
            themeToggle.setSelected(isDarkMode);
        }

        // Apply initial theme
        applyTheme();

        // Setup chat button (similar to Accueil)
        if (chatButton != null) {
            setupChatButton();
        }

        // Initialize sort options
        if (sortOrderComboBox != null) {
            sortOrderComboBox.setItems(FXCollections.observableArrayList(
                    "Plus récents d'abord",
                    "Plus anciens d'abord",
                    "Alphabétique (A-Z)",
                    "Alphabétique (Z-A)"
            ));
            sortOrderComboBox.setValue("Plus récents d'abord");
            sortOrderComboBox.setOnAction(e -> sortBlogs());
        }

        // Initialize search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> searchBlogs(newText));
        }

        int limit = Math.min(3, sorted.size());

        // Ajouter les 3 premiers blogs à la HBox
        for (int i = 0; i < limit; i++) {
            Blog b = sorted.get(i);
            recenthb.getChildren().add(createBlogCard(b));
        }

        // Ajouter les autres blogs à la VBox (défilement)
        for (int i = 3; i < sorted.size(); i++) {
            Blog b = sorted.get(i);
            toblogs.getChildren().add(createBlogHBox(b));
        }

        // Mettre à jour l'affichage des blogs
        updateBlogDisplay(sorted);
    }

    @FXML
    private void handleThemeToggle(ActionEvent event) {
        isDarkMode = themeToggle.isSelected();
        themeToggle.setText(isDarkMode ? "Light Mode" : "Dark Mode");
        applyTheme();
    }

    private void applyTheme() {
        Scene scene = themeToggle != null ? themeToggle.getScene() : null;
        if (scene != null) {
            if (isDarkMode) {
                // Apply Dark Mode styles
                scene.getRoot().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
                // Update blog cards and HBox items for dark mode
                updateBlogItemsTheme(true);
            } else {
                // Apply Light Mode styles
                scene.getRoot().setStyle("-fx-background-color: white; -fx-text-fill: black;");
                // Update blog cards and HBox items for light mode
                updateBlogItemsTheme(false);
            }
        }
    }

    private void updateBlogItemsTheme(boolean isDark) {
        // Update recent blogs cards
        for (Node card : recenthb.getChildren()) {
            if (card instanceof AnchorPane) {
                if (isDark) {
                    card.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555;");

                    // Update labels within the card
                    for (Node child : ((AnchorPane) card).getChildren()) {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            if (label.getText().startsWith("Description:") || label.getText().startsWith("Créé le:")) {
                                label.setTextFill(Color.LIGHTGRAY);
                            }
                        }
                    }
                } else {
                    card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd;");

                    // Reset labels to original colors
                    for (Node child : ((AnchorPane) card).getChildren()) {
                        if (child instanceof Label) {
                            Label label = (Label) child;
                            if (label.getText().startsWith("Description:")) {
                                label.setTextFill(Color.web("#555555"));
                            } else if (label.getText().startsWith("Créé le:")) {
                                label.setTextFill(Color.web("#777777"));
                            }
                        }
                    }
                }
            }
        }

        // Update all blogs in the VBox
        for (Node hbox : toblogs.getChildren()) {
            if (hbox instanceof HBox) {
                if (isDark) {
                    hbox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5;");

                    // Update labels within the HBox
                    for (Node child : ((HBox) hbox).getChildren()) {
                        if (child instanceof VBox) {
                            for (Node vboxChild : ((VBox) child).getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().startsWith("Description :")) {
                                        label.setTextFill(Color.LIGHTGRAY);
                                    } else if (label.getText().startsWith("Créé le :")) {
                                        label.setTextFill(Color.LIGHTGRAY);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    hbox.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

                    // Reset labels to original colors
                    for (Node child : ((HBox) hbox).getChildren()) {
                        if (child instanceof VBox) {
                            for (Node vboxChild : ((VBox) child).getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().startsWith("Description :")) {
                                        label.setTextFill(Color.web("#555555"));
                                    } else if (label.getText().startsWith("Créé le :")) {
                                        label.setTextFill(Color.web("#777777"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateBlogDisplay(List<Blog> blogs) {
        toblogs.getChildren().clear();
        for (Blog b : blogs) {
            toblogs.getChildren().add(createBlogHBox(b));
        }
    }

    private void searchBlogs(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            updateBlogDisplay(getBlogsFromDB());
            return;
        }

        List<Blog> filteredBlogs = getBlogsFromDB().stream()
                .filter(blog ->
                        blog.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                                blog.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        updateBlogDisplay(filteredBlogs);
    }

    private void sortBlogs() {
        String sortOption = sortOrderComboBox.getValue();
        List<Blog> blogs = getBlogsFromDB();

        if (sortOption != null) {
            switch (sortOption) {
                case "Plus récents d'abord":
                    blogs = blogs.stream()
                            .sorted(Comparator.comparingInt(Blog::getId).reversed())
                            .collect(Collectors.toList());
                    break;
                case "Plus anciens d'abord":
                    blogs = blogs.stream()
                            .sorted(Comparator.comparingInt(Blog::getId))
                            .collect(Collectors.toList());
                    break;
                case "Alphabétique (A-Z)":
                    blogs = blogs.stream()
                            .sorted(Comparator.comparing(Blog::getTitle))
                            .collect(Collectors.toList());
                    break;
                case "Alphabétique (Z-A)":
                    blogs = blogs.stream()
                            .sorted(Comparator.comparing(Blog::getTitle).reversed())
                            .collect(Collectors.toList());
                    break;
            }
        }

        updateBlogDisplay(blogs);
    }

    private List<Blog> getBlogsFromDB() {
        ObservableList<Blog> blogs = FXCollections.observableArrayList();
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM blog ORDER BY id DESC");
            while (rs.next()) {
                Blog blog = new Blog(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("created_at_blog"),
                        rs.getTimestamp("updated_at_blog") != null ? String.valueOf(rs.getTimestamp("updated_at_blog").toLocalDateTime()) : null
                );
                blogs.add(blog);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de récupération des blogs");
        }

        return blogs.stream()
                .sorted(Comparator.comparingInt(Blog::getId).reversed())
                .collect(Collectors.toList());
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

    private AnchorPane createBlogCard(Blog b) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(275.0, 140.0);
        card.getStyleClass().add("blog-card");
        card.setEffect(new DropShadow());
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #dddddd;");

        Label titleLabel = new Label(b.getTitle());
        titleLabel.setFont(Font.font("Berlin Sans FB", 14.0));
        titleLabel.setTextFill(Color.web("#2262c6"));
        titleLabel.setLayoutX(10.0);
        titleLabel.setLayoutY(10.0);

        Label descriptionLabel = new Label("Description: " + b.getDescription());
        descriptionLabel.setFont(Font.font("Arial", 12.0));
        descriptionLabel.setTextFill(Color.web("#555555"));
        descriptionLabel.setLayoutX(10.0);
        descriptionLabel.setLayoutY(35.0);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(255.0);

        Label dateLabel = new Label("Créé le: " + b.getCreatedAtBlog().toString());
        dateLabel.setFont(Font.font("Arial", 12.0));
        dateLabel.setTextFill(Color.web("#777777"));
        dateLabel.setLayoutX(10.0);
        dateLabel.setLayoutY(100.0);

        card.getChildren().addAll(titleLabel, descriptionLabel, dateLabel);

        card.setOnMouseEntered(e -> {
            if (isDarkMode) {
                card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #555555;");
            } else {
                card.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #dddddd;");
            }
        });
        card.setOnMouseExited(e -> {
            if (isDarkMode) {
                card.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #555555;");
            } else {
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #dddddd;");
            }
        });

        return card;
    }

    private HBox createBlogHBox(Blog blog) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(15.0);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label titleLabel = new Label("Titre : " + blog.getTitle());
        titleLabel.setTextFill(Color.web("#2262c6"));
        titleLabel.setFont(Font.font("Berlin Sans FB", 14.0));

        Label descriptionLabel = new Label("Description : " + blog.getDescription());
        descriptionLabel.setTextFill(Color.web("#555555"));
        descriptionLabel.setFont(Font.font("Arial", 13.0));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        Label dateLabel = new Label("Créé le : " + blog.getCreatedAtBlog().toString());
        dateLabel.setTextFill(Color.web("#777777"));
        dateLabel.setFont(Font.font("Arial", 12.0));

        VBox textBox = new VBox(titleLabel, descriptionLabel, dateLabel);
        textBox.setSpacing(4);

        // Edit button
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2262c6; -fx-text-fill: white; -fx-background-radius: 5;");

        // Delete button
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-background-radius: 5;");

        VBox buttonBox = new VBox(editButton, deleteButton);
        buttonBox.setSpacing(5);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 0, 10));

        HBox.setMargin(buttonBox, new Insets(0, 0, 0, 20));

        hbox.getChildren().addAll(textBox, buttonBox);

        hbox.setOnMouseEntered(e -> {
            if (isDarkMode) {
                hbox.setStyle("-fx-background-color: #4a4a4a; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                hbox.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });
        hbox.setOnMouseExited(e -> {
            if (isDarkMode) {
                hbox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                hbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });

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
    private void goToBlogPage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/BlogPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMyDrivePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherBlog.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPost.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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

    private void setupChatButton() {
        // Configuration du drag and drop
        chatButton.setOnMousePressed(event -> {
            xOffset = event.getSceneX() - chatButton.getLayoutX();
            yOffset = event.getSceneY() - chatButton.getLayoutY();
            wasDragged = false;
            event.consume();
        });

        chatButton.setOnMouseDragged(event -> {
            wasDragged = true;

            double newX = event.getSceneX() - xOffset;
            double newY = event.getSceneY() - yOffset;

            // Limites pour ne pas sortir de l'écran
            newX = Math.max(0, Math.min(newX, chatButton.getParent().getLayoutBounds().getWidth() - chatButton.getWidth()));
            newY = Math.max(0, Math.min(newY, chatButton.getParent().getLayoutBounds().getHeight() - chatButton.getHeight()));

            chatButton.setLayoutX(newX);
            chatButton.setLayoutY(newY);
            event.consume();
        });

        chatButton.setOnMouseReleased(event -> {
            if (wasDragged) {
                snapToEdge(); // Coller au bord le plus proche
            }
            event.consume();
        });

        chatButton.setOnMouseClicked(event -> {
            if (!wasDragged && event.getButton() == MouseButton.PRIMARY) {
                handleOpenChat(new ActionEvent(chatButton, null));
            }
            event.consume();
        });
    }

    private void snapToEdge() {
        double parentWidth = chatButton.getParent().getLayoutBounds().getWidth();
        double buttonWidth = chatButton.getWidth();
        double currentX = chatButton.getLayoutX();
        double currentY = chatButton.getLayoutY();

        // Déterminer le bord le plus proche
        boolean snapToRight = currentX > parentWidth / 2;
        double targetX = snapToRight ? parentWidth - buttonWidth : 0;

        // Garder la position Y actuelle (ou ajuster si nécessaire)
        double targetY = Math.max(20, Math.min(currentY,
                chatButton.getParent().getLayoutBounds().getHeight() - chatButton.getHeight() - 20));

        // Animation avec rebond
        Timeline timeline = new Timeline();

        // Animation principale
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(chatButton.layoutXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY, Interpolator.EASE_OUT))
        );

        // Premier rebond
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(chatButton.layoutXProperty(), snapToRight ? targetX - 25 : targetX + 25, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY + 15, Interpolator.EASE_OUT))
        );

        // Position finale
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(500),
                        new KeyValue(chatButton.layoutXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY, Interpolator.EASE_OUT))
        );

        timeline.play();
    }

    @FXML
    private void handleOpenChat(ActionEvent event) {
        // Vérifier si le chat est déjà ouvert
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage && "Chat en ligne".equals(((Stage) window).getTitle())) {
                window.requestFocus();
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Parent root = loader.load();

            // Créer le stage sans bordure
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            // Créer la scène avec un fond transparent
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setTitle("Chat en ligne");

            // Afficher la fenêtre
            stage.show();

            // Positionner en bas à droite de l'écran
            positionChatWindowBottomRight(stage);

            // Animation d'ouverture
            root.setScaleX(0.3);
            root.setScaleY(0.3);
            root.setOpacity(0);

            // Animation d'ouverture
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), root);
            scaleIn.setFromX(0.3);
            scaleIn.setFromY(0.3);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
            parallelTransition.setInterpolator(Interpolator.EASE_OUT);
            parallelTransition.play();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le chat: " + e.getMessage());
            alert.show();
        }
    }

    /**
     * Positionne la fenêtre de chat en bas à droite de l'écran
     */
    private void positionChatWindowBottomRight(Stage stage) {
        // Obtenir les dimensions de l'écran
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();

        // Attendre que le stage soit complètement chargé pour obtenir ses dimensions réelles
        stage.setOnShown(e -> {
            // Calculer la position pour que la fenêtre soit en bas à droite
            double rightPosition = screenBounds.getMaxX() - stage.getWidth() - 20;  // 20px de marge
            double bottomPosition = screenBounds.getMaxY() - stage.getHeight() - 50; // 50px de marge

            // Positionner la fenêtre
            stage.setX(rightPosition);
            stage.setY(bottomPosition);
        });
    }
    @FXML
    private void goToUserPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de gestion des utilisateurs.");
        }
    }
    // Méthode pour gérer la déconnexion
    @FXML
    private void handleLogoutAction(ActionEvent event) {
        // Clear the session
        SessionManager.logout();

        // Redirect to the login screen
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

    // Méthode pour naviguer vers le profil
    @FXML
    private void navigateToProfile(ActionEvent event) {
        try {
            // Load the profile dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profileDialog.fxml"));
            Parent root = loader.load();

            // Create a new stage for the dialog
            Stage profileStage = new Stage();
            profileStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            profileStage.initStyle(StageStyle.DECORATED);
            profileStage.setTitle("Modifier le profil");
            profileStage.setScene(new Scene(root));

            // Set position relative to the main window
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setX(primaryStage.getX() + 100);
            profileStage.setY(primaryStage.getY() + 100);

            // Show the dialog and wait for it to close
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de profil: " + e.getMessage());
        }
    }
    @FXML
    private void goToPostPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PostF.fxml"));
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
}