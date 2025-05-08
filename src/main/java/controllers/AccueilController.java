package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccueilController implements Initializable {

    @FXML
    private ToggleButton themeToggle; // Toggle button for Dark/Light mode
    @FXML
    private Button chatButton;
    private Timeline chatPulseTimeline;
    @FXML
    private Text pageTitle;
    @FXML
    private Button addButton;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean wasDragged = false;

    private boolean isDarkMode = false; // Track the current mode (default to Light Mode)


    @FXML

    private Stage chatStage;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize components and settings
        if (themeToggle != null) {
            themeToggle.setText("Dark Mode"); // Initial text
            themeToggle.setSelected(isDarkMode);
        }

        // Apply initial theme
        applyTheme();
        setupChatButton();

    }

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
            showAlert("Erreur", "Impossible de se déconnecter.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleThemeToggle(ActionEvent event) {
        isDarkMode = themeToggle.isSelected(); // Get the toggle state

        // Update the toggle button text
        themeToggle.setText(isDarkMode ? "Light Mode" : "Dark Mode");

        // Apply the selected theme
        applyTheme();
    }

    private void applyTheme() {
        Scene scene = themeToggle != null ? themeToggle.getScene() : null;
        if (scene != null) {
            if (isDarkMode) {
                // Apply Dark Mode styles
                scene.getRoot().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
            } else {
                // Apply Light Mode styles
                scene.getRoot().setStyle("-fx-background-color: white; -fx-text-fill: black;");
            }
        }
    }
    @FXML
    private void goToBlogPage(ActionEvent event) {
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
    @FXML
    private void openChat() {
        System.out.println("Tentative d'ouverture du chat");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Parent root = loader.load();

            // Stocke le contrôleur principal pour les notifications
            root.setUserData(this);

            Stage chatStage = new Stage();
            Scene scene = new Scene(root);

            // Configure le style de la fenêtre
            ChatController.configureStage(chatStage);

            chatStage.setScene(scene);
            chatStage.setTitle("Edunova Chat");
            chatStage.show();

            // Récupère le controller pour configurer les handlers de fermeture
            ChatController controller = loader.getController();
            chatStage.setOnCloseRequest(e -> {
                controller.dispose();
            });

            System.out.println("Chat ouvert avec succès");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du chat: " + e.getMessage());
            // Afficher une alerte d'erreur
        }
    }
    // Méthode appelée par ChatController pour les notifications
    public void addNotification(String message) {
        // Implémentez ici votre système de notification
        // Par exemple, ajouter à une liste de notifications
        System.out.println("Notification reçue: " + message);

        // Vous pourriez afficher une petite popup, augmenter un compteur, etc.
    }
    private void setupChatButton() {
        // Animation de pulsation (déjà configurée dans setupChatButtonPulse)

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
    private void navigateToProfile(ActionEvent event) {
        // Instead of just showing user details in an alert, open a profile editing dialog
        try {
            // Load the profile dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profileDialog.fxml"));
            Parent root = loader.load();

            // Create a new stage for the dialog
            Stage profileStage = new Stage();
            profileStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            profileStage.initStyle(StageStyle.DECORATED);
            profileStage.setTitle("Edit Profile");
            profileStage.setScene(new Scene(root));

            // Set position relative to the main window
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setX(primaryStage.getX() + 100);
            profileStage.setY(primaryStage.getY() + 100);

            // Show the dialog and wait for it to close
            profileStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open profile dialog: " + e.getMessage(), Alert.AlertType.ERROR);
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


}