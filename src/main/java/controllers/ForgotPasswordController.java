package controllers;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.EmailService;
import service.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ForgotPasswordController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button sendButton;

    @FXML
    private VBox formContainer;

    @FXML
    private VBox successContainer;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();

        // Initialement, masquer le conteneur de succès
        if (successContainer != null) {
            successContainer.setVisible(false);
            successContainer.setManaged(false);
        }

        // Ajouter un écouteur pour valider l'email en temps réel
        if (emailField != null) {
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateEmail(newValue);
            });
        }
    }

    /**
     * Valide l'adresse email et met à jour l'interface en conséquence
     */
    private void validateEmail(String email) {
        boolean isValid = email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

        if (sendButton != null) {
            sendButton.setDisable(!isValid);
        }

        if (!isValid && email != null && !email.isEmpty()) {
            statusLabel.setText("Veuillez entrer une adresse email valide");
            statusLabel.setStyle("-fx-text-fill: red;");
        } else {
            statusLabel.setText("");
        }
    }

    /**
     * Gère l'envoi d'un nouveau mot de passe par email
     */
    @FXML
    private void handleSendEmail() {
        String email = emailField.getText().trim();

        // Désactiver le bouton pendant le traitement
        sendButton.setDisable(true);
        sendButton.setText("Envoi en cours...");

        // Créer une pause pour simuler le traitement et éviter le blocage de l'interface
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            try {
                processSendEmail(email);
            } catch (Exception e) {
                showMessage("Une erreur s'est produite: " + e.getMessage(), "error");
                e.printStackTrace();
            } finally {
                // Réactiver le bouton
                sendButton.setDisable(false);
                sendButton.setText("Envoyer");
            }
        });
        pause.play();
    }

    /**
     * Traite l'envoi d'email pour la réinitialisation du mot de passe
     */
    private void processSendEmail(String email) {
        try {
            // Vérifier si l'email existe dans la base de données
            if (userService.emailExists(email)) {
                // Génération et envoi d'un nouveau mot de passe par email
                String newPassword = EmailService.generateRandomPassword(10);
                String hashedPassword = userService.hashPassword(newPassword);

                // Mettre à jour le mot de passe dans la base de données
                if (userService.resetPassword(email, newPassword)) {
                    // Envoyer l'email avec le nouveau mot de passe
                    EmailService.sendNewPassword(email, newPassword);

                    // Afficher le message de succès
                    if (successContainer != null && formContainer != null) {
                        formContainer.setVisible(false);
                        formContainer.setManaged(false);
                        successContainer.setVisible(true);
                        successContainer.setManaged(true);
                    } else {
                        showMessage("Un email avec votre nouveau mot de passe a été envoyé!", "success");

                        // Rediriger vers la page de connexion après 3 secondes
                        PauseTransition redirectPause = new PauseTransition(Duration.seconds(3));
                        redirectPause.setOnFinished(e -> handleBackToLogin(new ActionEvent()));
                        redirectPause.play();
                    }
                } else {
                    showMessage("Erreur lors de la réinitialisation du mot de passe.", "error");
                }
            } else {
                showMessage("Aucun compte n'est associé à cette adresse email.", "error");
            }
        } catch (SQLException e) {
            showMessage("Erreur de connexion à la base de données: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    /**
     * Retour à la page de connexion
     */
    @FXML
    public void handleBackToLogin(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion - Edunova");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Erreur lors du retour à la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche un message d'erreur ou de succès dans le label de statut
     * @param message Le message à afficher
     * @param type Le type de message ("error" ou "success")
     */
    private void showMessage(String message, String type) {
        statusLabel.setText(message);
        if ("error".equals(type)) {
            statusLabel.setStyle("-fx-text-fill: red;");
        } else {
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }
}