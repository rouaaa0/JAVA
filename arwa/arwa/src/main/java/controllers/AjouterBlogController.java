package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Blog;
import service.BlogService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AjouterBlogController {

    @FXML
    private TextField TitleTextField;

    @FXML
    private TextField DescriptionTextField;

    @FXML
    private DatePicker CreatedAtBlogPicker;

    @FXML
    private DatePicker UpdatedAtBlogPicker;

    @FXML
    private Label TitleErrorLabel;

    @FXML
    private Label DescriptionErrorLabel;

    @FXML
    private Button AjouterButton;

    private Runnable refreshCallback;

    /**
     * Sets the refresh callback to be called after a blog is added.
     * @param refreshCallback A callback function to refresh the list of blogs.
     */
    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    /**
     * Action handler for the "Ajouter" button.
     * Validates the input, retrieves the dates from the DatePickers, and saves the blog.
     */
    @FXML
    void ajouterAction(ActionEvent event) {
        clearErrors();

        // Retrieve and validate input fields
        String title = TitleTextField.getText().trim();
        String description = DescriptionTextField.getText().trim();

        boolean hasError = false;

        if (title.isEmpty()) {
            TitleErrorLabel.setText("Titre requis");
            hasError = true;
        } else if (title.length() < 3) {
            TitleErrorLabel.setText("Min. 3 caractères");
            hasError = true;
        }

        if (description.isEmpty()) {
            DescriptionErrorLabel.setText("Description requise");
            hasError = true;
        } else if (description.length() < 10) {
            DescriptionErrorLabel.setText("Min. 10 caractères");
            hasError = true;
        }

        if (hasError) return;

        // Retrieve dates from DatePickers
        LocalDate createdAtBlog = CreatedAtBlogPicker.getValue();
        LocalDate updatedAtBlog = UpdatedAtBlogPicker.getValue();

        // Validate that dates are selected
        if (createdAtBlog == null || updatedAtBlog == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Dates manquantes");
            alert.setContentText("Veuillez sélectionner les dates pour 'Created At' et 'Updated At'.");
            alert.showAndWait();
            return;
        }

        // Format dates as Strings
        String createdAtBlogStr = createdAtBlog.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String updatedAtBlogStr = updatedAtBlog.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        BlogService blogService = new BlogService();

        // Create a new Blog object with the provided data
        Blog blog = new Blog(title, description, createdAtBlogStr, updatedAtBlogStr);

        try {
            // Save the blog using the service
            blogService.add(blog);

            // Trigger the refresh callback
            if (refreshCallback != null) {
                refreshCallback.run();
            }

            // Optional: Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Blog ajouté avec succès !");
            alert.showAndWait();

            // Close the current stage
            Stage stage = (Stage) AjouterButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            // Handle SQL exception and show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ajout");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Clears all error messages from the UI.
     */
    private void clearErrors() {
        TitleErrorLabel.setText("");
        DescriptionErrorLabel.setText("");
    }
}