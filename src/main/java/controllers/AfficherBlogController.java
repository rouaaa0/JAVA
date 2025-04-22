package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Blog;
import utils.MyDataBase;

import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.input.KeyEvent;
import java.util.List;
import java.util.ArrayList;


public class AfficherBlogController {

    @FXML
    private FlowPane cardContainer;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker createdAtPicker;

    @FXML
    private DatePicker updatedAtPicker;

   /* @FXML
    private ImageView blogImagePreview;*/

    private List<Blog> blogList = new ArrayList<>();
    private Blog selectedBlog = null; // To keep track of the selected blog
    private String blogImagePath = null; // To store the path of the uploaded image
    @FXML
    private TextField searchField;

    // Add these with your other class variables
    private boolean isSortedAscending = true;
    private String currentSortField = "title"; // default sort by title
    private static final List<String> blogHistoryLogs = new ArrayList<>();
    @FXML
    private TextArea historyArea; // Add this in your FXML file
    @FXML
    private VBox historySection; // Reference to the VBox for history
    private int currentPage = 1; // Keeps track of the current page
    private final int postsPerPage = 5; // Number of posts per page
    private int totalPosts = 0; // Total number of posts

    @FXML
    private Label currentPageLabel; // Label to display the current page
    @FXML
    private Button prevPageButton, nextPageButton; // Buttons for pagination
    private boolean isAscending = true; // Track the current sort order

    @FXML
    public void initialize() {
        // Fetch blogs from the database
        fetchBlogsFromDatabase();

        // Refresh the card view to display the blogs
        refreshCards();
    }

    /**
     * Fetches blogs from the database and populates the blog list.
     */
    /*private void fetchBlogsFromDatabase() {
        blogList.clear(); // Clear the current list before fetching
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "SELECT * FROM blog";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Blog blog = new Blog(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created_at_blog") != null
                                ? String.valueOf(resultSet.getTimestamp("created_at_blog").toLocalDateTime())
                                : null,
                        resultSet.getTimestamp("updated_at_blog") != null
                                ? String.valueOf(resultSet.getTimestamp("updated_at_blog").toLocalDateTime())
                                : null
                );
                blogList.add(blog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load blogs: " + e.getMessage());
        }
    }*/
    private void fetchBlogsFromDatabase() {
        blogList.clear(); // Clear the current list before fetching
        try {
            Connection connection = MyDataBase.getInstance().getConnection();

            // Query to count the total number of blogs (for pagination)
            String countQuery = "SELECT COUNT(*) FROM blog";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();
            if (countResult.next()) {
                totalPosts = countResult.getInt(1); // Update the total number of blogs
            }

            // Paginated query to fetch blogs for the current page
            String query = "SELECT * FROM blog LIMIT ? OFFSET ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postsPerPage); // Limit (number of blogs per page)
            preparedStatement.setInt(2, (currentPage - 1) * postsPerPage); // Offset (starting point for the page)

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Blog blog = new Blog(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created_at_blog") != null
                                ? String.valueOf(resultSet.getTimestamp("created_at_blog").toLocalDateTime())
                                : null,
                        resultSet.getTimestamp("updated_at_blog") != null
                                ? String.valueOf(resultSet.getTimestamp("updated_at_blog").toLocalDateTime())
                                : null
                );
                blogList.add(blog);
            }

            // Refresh the UI to display the newly fetched blogs
            refreshCards();
            updatePaginationControls(); // Update pagination buttons and label

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load blogs: " + e.getMessage());
        }
    }

    /**
     * Refreshes the visual cards representing blogs in the UI.
     */
   /* private void refreshCards() {
        cardContainer.getChildren().clear();

        for (Blog blog : blogList) {
            VBox card = createBlogCard(blog);
            cardContainer.getChildren().add(card);
        }
    }*/
    private void refreshCards() {
        // Clear the current content in the card container
        cardContainer.getChildren().clear();

        // Create and add a card for each blog in the blogList
        blogList.forEach(blog -> {
            VBox card = createBlogCard(blog); // Create a visual card for the blog
            cardContainer.getChildren().add(card); // Add the card to the container
        });

        // Display a message if there are no blogs available
        if (blogList.isEmpty()) {
            Label noBlogsLabel = new Label("No blogs to display.");
            noBlogsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-font-weight: bold;");
            cardContainer.getChildren().add(noBlogsLabel);
        }
    }

    /**
     * Creates a visual card for a given blog.
     */
    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #E8F0FE; -fx-padding: 10; -fx-border-color: #004AAD; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setPrefWidth(150);

        Text titleText = new Text("Title: " + blog.getTitle());
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text descriptionText = new Text("Description: " + blog.getDescription());
        Text createdAtText = new Text("Created At: " + blog.getCreatedAtBlog());
        Text updatedAtText = new Text("Updated At: " + blog.getUpdatedAtBlog());

        // Set an onMouseClicked event to populate the form when the card is clicked
        card.setOnMouseClicked(event -> {
            selectedBlog = blog;
            populateFields(blog);
        });

        card.getChildren().addAll(titleText, descriptionText, createdAtText, updatedAtText);

        return card;
    }

    /**
     * Populates the form fields with the selected blog's data.
     */
    private void populateFields(Blog blog) {
        titleField.setText(blog.getTitle());
        descriptionField.setText(blog.getDescription());
        createdAtPicker.setValue(blog.getCreatedAtBlog() != null ? java.time.LocalDate.parse(blog.getCreatedAtBlog().substring(0, 10)) : null);
        updatedAtPicker.setValue(blog.getUpdatedAtBlog() != null ? java.time.LocalDate.parse(blog.getUpdatedAtBlog().substring(0, 10)) : null);
    }

    /**
     * Displays an alert dialog with the given title and content.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Clears the form fields and resets the selected blog.
     */
    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        createdAtPicker.setValue(null);
        updatedAtPicker.setValue(null);
        //blogImagePreview.setImage(null);
        blogImagePath = null;
        selectedBlog = null;
    }

    /**
     * Handles the Add button action with validation checks.
     */
    @FXML
    private void handleAddAction(ActionEvent event) {
        // Validate the form fields
        if (!validateForm()) {
            return;
        }

        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "INSERT INTO blog (title, description, created_at_blog, updated_at_blog) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, titleField.getText());
            preparedStatement.setString(2, descriptionField.getText());
            preparedStatement.setDate(3, java.sql.Date.valueOf(createdAtPicker.getValue()));
            preparedStatement.setDate(4, java.sql.Date.valueOf(updatedAtPicker.getValue()));
            preparedStatement.executeUpdate();

            // Retrieve the ID of the newly added blog
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int blogId = generatedKeys.getInt(1);

                // Add the action to the history logs
                blogHistoryLogs.add("Blog added: '" + titleField.getText() + "' (ID: " + blogId + ")");
            }

            showAlert("Success", "Blog added successfully!");
            fetchBlogsFromDatabase();
            refreshCards();
            clearForm();

        } catch (Exception e) {
            showAlert("Error", "An error occurred while adding the blog: " + e.getMessage());
        }
    }

    /**
     * Handles the Update button action with validation and database update.
     */
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (selectedBlog == null) {
            showAlert("Error", "Please select a blog to update.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "UPDATE blog SET title = ?, description = ?, created_at_blog = ?, updated_at_blog = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, titleField.getText());
            preparedStatement.setString(2, descriptionField.getText());
            preparedStatement.setDate(3, java.sql.Date.valueOf(createdAtPicker.getValue()));
            preparedStatement.setDate(4, java.sql.Date.valueOf(updatedAtPicker.getValue()));
            preparedStatement.setInt(5, selectedBlog.getId());
            preparedStatement.executeUpdate();

            // Add the action to the blog history logs
            blogHistoryLogs.add("Blog updated: '" + selectedBlog.getTitle() + "' (ID: " + selectedBlog.getId() + ")");

            showAlert("Success", "Blog updated successfully!");
            fetchBlogsFromDatabase();
            refreshCards();
            clearForm();

        } catch (Exception e) {
            showAlert("Error", "An error occurred while updating the blog: " + e.getMessage());
        }
    }
    /**
     * Handles the Delete button action with a confirmation dialog.
     */
    @FXML
    private void handleDeleteAction() {
        if (selectedBlog != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this blog?");
            confirmationAlert.setContentText("Blog Title: " + selectedBlog.getTitle());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        Connection connection = MyDataBase.getInstance().getConnection();
                        String query = "DELETE FROM blog WHERE id = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, selectedBlog.getId());
                        preparedStatement.executeUpdate();

                        // Add the action to the blog history logs
                        blogHistoryLogs.add("Blog deleted: '" + selectedBlog.getTitle() + "' (ID: " + selectedBlog.getId() + ")");

                        showAlert("Success", "Blog deleted successfully!");
                        fetchBlogsFromDatabase();
                        refreshCards();
                        clearForm();

                    } catch (Exception e) {
                        showAlert("Error", "An error occurred while deleting the blog: " + e.getMessage());
                    }
                } else {
                    showAlert("Action Canceled", "Blog deletion was canceled.");
                }
            });
        } else {
            showAlert("No Selection", "Please select a blog to delete.");
        }
    }
    /**
     * Handles the image upload for the blog.
     */
  /*  @FXML
    private void handleBlogImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Blog Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(blogImagePreview.getScene().getWindow());

        if (selectedFile != null) {
            try {
                blogImagePath = selectedFile.getAbsolutePath();
                Image image = new Image(selectedFile.toURI().toString());
                blogImagePreview.setImage(image);
                showAlert("Success", "Image uploaded successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    /**
     * Validates the form fields.
     */
    private boolean validateForm() {
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert("Validation Error", "The title must not be empty.");
            return false;
        }
        if (title.length() < 3) {
            showAlert("Validation Error", "The title must have at least 3 characters.");
            return false;
        }

        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            showAlert("Validation Error", "The description must not be empty.");
            return false;
        }
        if (description.length() < 10) {
            showAlert("Validation Error", "The description must have at least 10 characters.");
            return false;
        }

        if (createdAtPicker.getValue() == null) {
            showAlert("Validation Error", "Please select a creation date.");
            return false;
        }

        if (updatedAtPicker.getValue() == null) {
            showAlert("Validation Error", "Please select an update date.");
            return false;
        }

        return true;
    }
    /**
     * Filters blogs by title based on search text
     */
    private void filterBlogsByTitle(String searchText) {
        cardContainer.getChildren().clear();

        List<Blog> filtered = blogList.stream()
                .filter(blog ->
                        searchText == null ||
                                searchText.isEmpty() ||
                                blog.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        filtered.forEach(blog -> {
            VBox card = createBlogCard(blog);
            cardContainer.getChildren().add(card);
        });
    }
    /**
     * Displays only the filtered blogs in the card view
     */
    private void displayFilteredBlogs(List<Blog> filteredBlogs) {
        cardContainer.getChildren().clear();

        for (Blog blog : filteredBlogs) {
            VBox card = createBlogCard(blog);
            cardContainer.getChildren().add(card);
        }
    }
    /*@FXML
    private void handleSearchAction() {
        String searchText = searchField.getText();
        filterBlogsByTitle(searchText);
    }*/
    @FXML
    private void handleSearchKeyReleased(KeyEvent event) {
        filterBlogsByTitle(searchField.getText());
    }
    @FXML
    private void handleViewHistory(ActionEvent event) {
        // Populate the history logs
        StringBuilder historyText = new StringBuilder();
        for (String log : blogHistoryLogs) {
            historyText.append(log).append("\n");
        }
        historyArea.setText(historyText.toString());

        // Show the history section
        historySection.setVisible(true);
        historySection.setManaged(true);
    }
    @FXML
    private void handleCloseHistory(ActionEvent event) {
        // Hide the history section
        historySection.setVisible(false);
        historySection.setManaged(false);
    }



    @FXML
    private void handlePrevPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--; // Move to the previous page
            fetchBlogsFromDatabase(); // Fetch blogs for the new page
        }
    }


    @FXML
    private void handleNextPage(ActionEvent event) {
        if (currentPage * postsPerPage < totalPosts) {
            currentPage++; // Move to the next page
            fetchBlogsFromDatabase(); // Fetch blogs for the new page
        }
    }

    private void updatePaginationControls() {
        currentPageLabel.setText("Page " + currentPage); // Update the page label
        prevPageButton.setDisable(currentPage == 1); // Disable "Previous" button if on the first page
        nextPageButton.setDisable(currentPage * postsPerPage >= totalPosts); // Disable "Next" button if on the last page
    }




    @FXML
    private void handleSortByTitle(ActionEvent event) {
        // Toggle the sort order
        isAscending = !isAscending;

        // Sort the blogList by title
        blogList.sort((blog1, blog2) -> {
            if (isAscending) {
                return blog1.getTitle().compareToIgnoreCase(blog2.getTitle());
            } else {
                return blog2.getTitle().compareToIgnoreCase(blog1.getTitle());
            }
        });

        // Refresh the cards to show the sorted blogs
        refreshCards();
    }
}