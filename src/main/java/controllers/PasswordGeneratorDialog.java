package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.PasswordGenerator;

/**
 * Dialog for generating secure passwords
 */
public class PasswordGeneratorDialog extends Dialog<String> {
    private TextField passwordField;
    private Slider lengthSlider;
    private CheckBox lowercaseCheckbox;
    private CheckBox uppercaseCheckbox;
    private CheckBox numbersCheckbox;
    private CheckBox specialCheckbox;
    private ProgressBar strengthBar;
    private Label strengthLabel;

    /**
     * Creates a new password generator dialog
     */
    public PasswordGeneratorDialog() {
        // Configure dialog
        setTitle("Générateur de Mot de Passe");
        setHeaderText("Créer un mot de passe sécurisé");

        // Set up the dialog
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> stage.close());

        // Create UI components
        setupDialogContent();

        // Add buttons
        ButtonType generateButtonType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        ButtonType useButtonType = new ButtonType("Utiliser", ButtonBar.ButtonData.APPLY);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(generateButtonType, useButtonType, cancelButtonType);

        // Set initial password
        generateNewPassword();

        // Handle button actions
        setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                generateNewPassword();
                return null;
            }
            if (dialogButton == useButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        // Add event handlers for password generation options
        setupEventHandlers(generateButtonType);
    }

    /**
     * Sets up the dialog content with all UI components
     */
    private void setupDialogContent() {
        // Password display field
        passwordField = new TextField();
        passwordField.setEditable(false);
        passwordField.setPrefWidth(300);

        // Copy button
        Button copyButton = new Button("Copier");
        copyButton.setOnAction(e -> {
            passwordField.selectAll();
            passwordField.copy();
        });

        // Create password display row
        HBox passwordBox = new HBox(10, passwordField, copyButton);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        // Password length slider
        lengthSlider = new Slider(6, 24, 12);
        lengthSlider.setShowTickLabels(true);
        lengthSlider.setShowTickMarks(true);
        lengthSlider.setMajorTickUnit(3);
        lengthSlider.setBlockIncrement(1);
        lengthSlider.setSnapToTicks(true);

        Label lengthValueLabel = new Label("12");
        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.intValue();
            lengthValueLabel.setText(String.valueOf(length));
        });

        // Create length controls row
        HBox lengthBox = new HBox(10, new Label("Longueur:"), lengthSlider, lengthValueLabel);
        lengthBox.setAlignment(Pos.CENTER_LEFT);

        // Character type checkboxes
        lowercaseCheckbox = new CheckBox("Minuscules (a-z)");
        lowercaseCheckbox.setSelected(true);

        uppercaseCheckbox = new CheckBox("Majuscules (A-Z)");
        uppercaseCheckbox.setSelected(true);

        numbersCheckbox = new CheckBox("Chiffres (0-9)");
        numbersCheckbox.setSelected(true);

        specialCheckbox = new CheckBox("Caractères spéciaux (!@#$%^&*)");
        specialCheckbox.setSelected(true);

        // Create character options grid
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(10);
        optionsGrid.setVgap(10);
        optionsGrid.add(lowercaseCheckbox, 0, 0);
        optionsGrid.add(uppercaseCheckbox, 1, 0);
        optionsGrid.add(numbersCheckbox, 0, 1);
        optionsGrid.add(specialCheckbox, 1, 1);

        // Password strength indicator
        strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(200);

        strengthLabel = new Label("Force: Faible");
        strengthLabel.setPrefWidth(100);

        HBox strengthBox = new HBox(10, new Label("Sécurité:"), strengthBar, strengthLabel);
        strengthBox.setAlignment(Pos.CENTER_LEFT);

        // Main layout
        VBox content = new VBox(15,
                passwordBox,
                new Separator(),
                lengthBox,
                new Separator(),
                optionsGrid,
                new Separator(),
                strengthBox);
        content.setPadding(new Insets(20));

        getDialogPane().setContent(content);
    }

    /**
     * Sets up event handlers for all UI components
     */
    private void setupEventHandlers(ButtonType generateButtonType) {
        // Checkbox event handlers
        CheckBox[] checkboxes = {lowercaseCheckbox, uppercaseCheckbox, numbersCheckbox, specialCheckbox};
        for (CheckBox checkbox : checkboxes) {
            checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Ensure at least one checkbox is selected
                if (!lowercaseCheckbox.isSelected() &&
                        !uppercaseCheckbox.isSelected() &&
                        !numbersCheckbox.isSelected() &&
                        !specialCheckbox.isSelected()) {
                    checkbox.setSelected(true);
                    return;
                }

                // Regenerate password when options change
                generateNewPassword();
            });
        }

        // Length slider event handler
        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.intValue() - oldVal.intValue()) >= 1) {
                generateNewPassword();
            }
        });
    }

    /**
     * Generates a new password based on current settings
     */
    private void generateNewPassword() {
        String password = PasswordGenerator.generatePassword(
                (int) lengthSlider.getValue(),
                lowercaseCheckbox.isSelected(),
                uppercaseCheckbox.isSelected(),
                numbersCheckbox.isSelected(),
                specialCheckbox.isSelected()
        );

        passwordField.setText(password);
        updatePasswordStrength(password);
    }

    /**
     * Updates the password strength indicator
     */
    private void updatePasswordStrength(String password) {
        int strength = PasswordGenerator.calculatePasswordStrength(password);
        strengthBar.setProgress(strength / 100.0);

        // Set color based on strength
        String color;
        String strengthText;

        if (strength < 40) {
            color = "red";
            strengthText = "Faible";
        } else if (strength < 70) {
            color = "orange";
            strengthText = "Moyen";
        } else {
            color = "green";
            strengthText = "Fort";
        }

        strengthBar.setStyle("-fx-accent: " + color + ";");
        strengthLabel.setText("Force: " + strengthText);
    }
}