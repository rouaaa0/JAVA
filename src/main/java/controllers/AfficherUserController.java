package controllers;

import javafx.animation.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import models.User;
import service.UserService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;


public class AfficherUserController {

    // FXML Fields
    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ImageView profilePicturePreview;
    @FXML private FlowPane cardContainer;
    @FXML private PasswordField passwordField;
    @FXML private PieChart rolePieChart; // PieChart for role statistics
    @FXML private Label errorLabel;
    @FXML private VBox roleStatisticsContainer; // VBox for role statistics
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Label pageNumberLabel;
    @FXML private TextField searchField; // Dynamic search field
    @FXML private ComboBox<String> sortOrderComboBox;
    @FXML private Label nameErrorLabel;
    @FXML private Label lastnameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label roleErrorLabel;
    @FXML private Label passwordErrorLabel;


    // Service and Data
    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private String profilePicturePath; // To store the selected profile picture path
    private User selectedUser; // To track the currently selected user

    // Pagination variables
    private int currentPage = 1; // Current page number
    private final int pageSize = 8; // Number of users per page

    @FXML
    public void initialize() {
        // Initialize ComboBox options for roles
        roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");

        // Initialize ComboBox options for sorting order
        sortOrderComboBox.getItems().addAll("Ascending", "Descending");

        // Load users initially without filtering or sorting (default to ascending order)
        loadUsersWithSorting("", "ASC");

        // Hide role statistics container initially
        roleStatisticsContainer.setVisible(false);
        roleStatisticsContainer.setManaged(false);

        // Add listener to the search field for dynamic filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1; // Reset to the first page on new search
            String sortOrder = sortOrderComboBox.getValue() != null && sortOrderComboBox.getValue().equals("Descending") ? "DESC" : "ASC";
            loadUsersWithSorting(newValue, sortOrder); // Apply search filter and sorting
        });

        // Add listener to the sort order ComboBox for dynamic sorting
        sortOrderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String sortOrder = newValue.equals("Descending") ? "DESC" : "ASC";
                loadUsersWithSorting(searchField.getText(), sortOrder); // Apply sorting to current search filter
            }
        });

    }
    private void loadUsers(String nameFilter) {
        try {
            // Calculate offset for the current page
            int offset = (currentPage - 1) * pageSize;

            // Fetch users based on pagination and search filter
            List<User> users = userService.selectWithPaginationAndSearch(pageSize, offset, nameFilter);

            // Clear existing users and add the new ones
            userList.clear();
            userList.addAll(users);

            // Display the filtered users as cards
            displayUsersAsCards();

            // Update page number label
            pageNumberLabel.setText("Page: " + currentPage);

            // Enable/Disable buttons based on page availability
            previousButton.setDisable(currentPage == 1);
            nextButton.setDisable(users.size() < pageSize);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayUsersAsCards() {
        cardContainer.getChildren().clear(); // Clear existing cards

        for (User user : userList) {
            // Create a VBox for each user card
            VBox card = new VBox(10);
            card.setStyle("-fx-border-color: #004AAD; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 5; -fx-padding: 5; -fx-background-color: #FFFFFF;");
            card.setPrefWidth(105); // Set card width

            // Add user details to the card
            Label nameLabel = new Label("Name: " + user.getName());
            nameLabel.setStyle("-fx-font-weight:bold ; -fx-text-fill: #004AAD;");

            Label lastnameLabel = new Label("Lastname: " + user.getLastname());
            lastnameLabel.setStyle("-fx-text-fill: #004AAD;");

            Label emailLabel = new Label("Email: " + user.getEmail());
            emailLabel.setStyle("-fx-text-fill: #004AAD;");

            Label roleLabel = new Label("Role: " + user.getRole());
            roleLabel.setStyle("-fx-text-fill: #004AAD;");

            // Add profile picture
            ImageView profilePic = new ImageView();
            profilePic.setFitWidth(50);
            profilePic.setFitHeight(50);
            profilePic.setPreserveRatio(true);
            if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
                File file = new File(user.getProfilepic());
                if (file.exists()) {
                    profilePic.setImage(new Image(file.toURI().toString()));
                }
            }

            // Add components to the card
            card.getChildren().addAll(profilePic, nameLabel, lastnameLabel, emailLabel, roleLabel);

            // Enable card click to select user
            card.setOnMouseClicked(event -> populateFormWithUser(user));

            // Add the card to the FlowPane
            cardContainer.getChildren().add(card);
        }
    }

    private void populateFormWithUser(User user) {
        // Populate the form fields with the selected user's data
        selectedUser = user; // Set the selected user
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());

        // Load the profile picture preview
        if (user.getProfilepic() != null && !user.getProfilepic().isEmpty()) {
            File file = new File(user.getProfilepic());
            if (file.exists()) {
                profilePicturePreview.setImage(new Image(file.toURI().toString()));
            } else {
                profilePicturePreview.setImage(null);
            }
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUsers(searchField.getText()); // Pass the current search term for filtering
        }
    }

    @FXML
    private void handleNextPage() {
        currentPage++;
        loadUsers(searchField.getText()); // Pass the current search term for filtering
    }

    private void loadRoleStatistics() {
        try {
            // Retrieve role counts from the database
            int adminCount = userService.getRoleCount("ROLE_ADMIN");
            int userCount = userService.getRoleCount("ROLE_USER");

            // Create PieChart data
            PieChart.Data adminData = new PieChart.Data("Admins", adminCount);
            PieChart.Data userData = new PieChart.Data("Users", userCount);

            // Clear old data and set new data
            rolePieChart.getData().clear();
            rolePieChart.getData().addAll(adminData, userData);
            rolePieChart.setTitle("Role Distribution");

        } catch (SQLException e) {
            errorLabel.setText("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStatistiqueAction(ActionEvent event) {
        try {
            // Refresh the statistics data
            loadRoleStatistics();

            // Make the statistics section visible
            roleStatisticsContainer.setVisible(true);
            roleStatisticsContainer.setManaged(true);
        } catch (Exception e) {
            showAlert("Error", "Failed to load statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleProfilePictureUpload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", ".png", ".jpg", ".jpeg", ".gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profilePicturePath = selectedFile.getAbsolutePath();
            profilePicturePreview.setImage(new Image(selectedFile.toURI().toString()));
        } else {
            showAlert("No File Selected", "Please select an image file for the profile picture.", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleAddAction(ActionEvent actionEvent) {
        // Validate Name
        if (nameField.getText().isEmpty() || !nameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Name must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Lastname
        if (lastnameField.getText().isEmpty() || !lastnameField.getText().matches("^[a-zA-Z]+$")) {
            showAlert("Validation Error", "Lastname must not be empty and should only contain letters.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Email
        if (emailField.getText().isEmpty() || !emailField.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showAlert("Validation Error", "Email must not be empty and should be a valid email address (e.g., user@example.com).", Alert.AlertType.WARNING);
            return;
        }

        // Validate Role
        if (roleComboBox.getValue() == null) {
            showAlert("Validation Error", "Role must be selected.", Alert.AlertType.WARNING);
            return;
        }

        // Validate Password
        String password = passwordField.getText().trim();
        if (!isValidPassword(password)) {
            showAlert("Validation Error", """
                Password must meet the following criteria:
                - At least 8 characters
                - At least one uppercase letter
                - At least one lowercase letter
                - At least one digit
                """, Alert.AlertType.WARNING);
            return;
        }

        // Hash the password
        String hashedPassword;
        try {
            hashedPassword = hashPassword(password);
        } catch (NoSuchAlgorithmException e) {
            showAlert("Error", "Could not hash the password: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Create a new User object
        User user = new User(
                nameField.getText().trim(),
                lastnameField.getText().trim(),
                emailField.getText().trim(),
                hashedPassword, // Store the hashed password
                roleComboBox.getValue(),
                profilePicturePath // Use the selected profile picture path
        );

        try {
            // Add the user to the database
            userService.add(user);

            // Add the user to the list and refresh cards
            userList.add(user);
            displayUsersAsCards();

            // Clear the form
            clearForm();

            // Show success message
            showAlert("Success", "User added successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not add user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Utility method to validate password
    private boolean isValidPassword(String password) {
        // Regex for password validation
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        return password.matches(passwordRegex);
    }

    // Utility method to hash passwords
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // Use SHA-256 for hashing
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b)); // Convert each byte to a hex value
        }
        return sb.toString();
    }

    @FXML
    public void handleUpdateAction(ActionEvent actionEvent) {
        if (selectedUser == null) {
            showAlert("No User Selected", "Please select a user to update.", Alert.AlertType.WARNING);
            return;
        }

        // Update only fields that are not empty
        if (!nameField.getText().trim().isEmpty()) selectedUser.setName(nameField.getText().trim());
        if (!lastnameField.getText().trim().isEmpty()) selectedUser.setLastname(lastnameField.getText().trim());
        if (!emailField.getText().trim().isEmpty()) selectedUser.setEmail(emailField.getText().trim());
        if (roleComboBox.getValue() != null) selectedUser.setRole(roleComboBox.getValue());
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) selectedUser.setProfilepic(profilePicturePath);

        // Save the updated user
        try {
            userService.update(selectedUser);
            displayUsersAsCards();
            clearForm();
            showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteAction(ActionEvent actionEvent) {
        if (selectedUser == null) {
            showAlert("No User Selected", "Please select a user to delete.", Alert.AlertType.WARNING);
            return;
        }

        // Confirm deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmationAlert.setContentText("User: " + selectedUser.getName() + " " + selectedUser.getLastname());
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(selectedUser.getId());
                    userList.remove(selectedUser);
                    displayUsersAsCards();
                    clearForm();
                    showAlert("Success", "User deleted successfully!", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void clearForm() {
        nameField.clear();
        lastnameField.clear();
        emailField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        profilePicturePreview.setImage(null);
        profilePicturePath = null;
        selectedUser = null;
        passwordField.clear();
    }

    private void loadUsersWithSorting(String nameFilter, String sortOrder) {
        try {
            // Calculate offset for the current page
            int offset = (currentPage - 1) * pageSize;

            // Fetch users with sorting and filtering
            List<User> users = userService.selectWithPaginationAndSorting(pageSize, offset, nameFilter, sortOrder);

            // Clear existing users and add the new ones
            userList.clear();
            userList.addAll(users);

            // Display the sorted users as cards
            displayUsersAsCards();

            // Update page number label
            pageNumberLabel.setText("Page: " + currentPage);

            // Enable/Disable buttons based on page availability
            previousButton.setDisable(currentPage == 1);
            nextButton.setDisable(users.size() < pageSize);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        SessionManager.logout(); // Clear the session
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading login screen: " + e.getMessage());
            showAlert("Error", "Failed to return to login screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void goToMyDrivePage(ActionEvent actionEvent) {
    }
    @FXML
    public void goToBlogPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherBlog.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate to Blog page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void goToPostPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPost.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate to Post page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void goToClubPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Club.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate to Club page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void goToEventPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Evenement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate to Event page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

}
