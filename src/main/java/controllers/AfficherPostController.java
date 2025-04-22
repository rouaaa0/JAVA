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
import models.Post;
import utils.MyDataBase;

import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import service.BlogService;
import service.PostService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import  javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.io.*;




public class AfficherPostController {

    @FXML
    private FlowPane cardContainer;

    @FXML
    private TextArea contentField;

    @FXML
    private DatePicker createdAtPicker;

    @FXML
    private DatePicker updatedAtPicker;

    @FXML
    private ImageView postImagePreview;

    @FXML
    private ComboBox<Blog> blogComboBox; // For blog selection

    private List<Post> postList = new ArrayList<>();
    private List<Blog> blogList = new ArrayList<>(); // To hold blogs for the ComboBox
    private Post selectedPost = null; // To keep track of the selected post
    private String postImagePath = null; // To store the path of the uploaded image
    private final BlogService blogService = new BlogService();
    private final PostService postService = new PostService();
    @FXML
    private PieChart statsPieChart;
    @FXML
    private TextField searchField;  // Add this with your other @FXML fields
   // private List<String> postHistoryLogs = new ArrayList<>();

    @FXML
    private VBox historySection; // Reference to the VBox for history
    @FXML
    private TextArea historyArea; // Reference to the TextArea for logs
    private static final String HISTORY_FILE_PATH = "postHistoryLogs.txt"; // File to store history logs
    private List<String> postHistoryLogs = new ArrayList<>();
    private int currentPage = 1; // Current page number
    private final int postsPerPage = 5; // Number of posts displayed per page
    private int totalPosts = 0; // Total number of posts in the database
    @FXML
    private Button prevPageButton, nextPageButton;
    private boolean isContentAscending = true; // Track the current sort order for content
    @FXML
    private Button sortContentButton;

   /* @FXML
    public void initialize() {
        // Fetch posts and blogs from the database
        fetchPostsFromDatabase();
        fetchBlogsFromDatabase();

        // Populate the blogComboBox
        populateBlogComboBox();

        // Refresh the card view to display the posts
        refreshCards();
    }*/
   @FXML
   public void initialize() {
       fetchBlogsFromDatabase(); // Fetch blogs for the ComboBox
       populateBlogComboBox(); // Populate the ComboBox with blogs

       fetchPostsFromDatabase(); // Fetch posts for the first page
       updatePaginationControls(); // Update pagination buttons
   }

    /**
     * Fetches posts from the database and populates the post list.
     */
    /*private void fetchPostsFromDatabase() {
        postList.clear(); // Clear the current list before fetching
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "SELECT * FROM post";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int blogId = resultSet.getInt("blog_id");
                Blog associatedBlog = blogList.stream().filter(blog -> blog.getId() == blogId).findFirst().orElse(null);

                Post post = new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("content"),
                        resultSet.getString("image"),
                        resultSet.getString("created_at"),
                        resultSet.getString("update_at"),
                        associatedBlog
                );
                postList.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load posts: " + e.getMessage());
        }
    }*/

    private void fetchPostsFromDatabase() {
        postList.clear(); // Clear the current list before fetching
        try {
            Connection connection = MyDataBase.getInstance().getConnection();

            // Query to count the total number of posts
            String countQuery = "SELECT COUNT(*) FROM post";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();
            if (countResult.next()) {
                totalPosts = countResult.getInt(1); // Update the total number of posts
            }

            // Paginated query to fetch posts for the current page
            String query = "SELECT * FROM post LIMIT ? OFFSET ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postsPerPage); // Limit (number of posts per page)
            preparedStatement.setInt(2, (currentPage - 1) * postsPerPage); // Offset (starting point for the page)

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int blogId = resultSet.getInt("blog_id");
                Blog associatedBlog = blogList.stream().filter(blog -> blog.getId() == blogId).findFirst().orElse(null);

                Post post = new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("content"),
                        resultSet.getString("image"),
                        resultSet.getString("created_at"),
                        resultSet.getString("update_at"),
                        associatedBlog
                );
                postList.add(post);
            }

            // Refresh the UI with the new list of posts
            refreshCards();
            updatePaginationControls(); // Update pagination buttons and label

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load posts: " + e.getMessage());
        }
    }

    /**
     * Fetches blogs from the database and populates the blog list.
     */
    private void fetchBlogsFromDatabase() {
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
                        resultSet.getString("created_at_blog"),
                        resultSet.getString("updated_at_blog")
                );
                blogList.add(blog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load blogs: " + e.getMessage());
        }
    }

    /**
     * Populates the blogComboBox with blogs.
     */
   /* private void populateBlogComboBox() {
        blogComboBox.getItems().clear();
        blogComboBox.getItems().addAll(blogList);
    }*/
    /**
     * Populates the blogComboBox with blogs and sets a custom cell factory to display only titles.
     */
    private void populateBlogComboBox() {
        blogComboBox.getItems().clear();
        blogComboBox.getItems().addAll(blogList);

        // Set a custom cell factory to display only the blog title
        blogComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Blog blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setText(null);
                } else {
                    setText(blog.getTitle()); // Show only the title
                }
            }
        });

        // Set the button cell to show the selected blog's title
        blogComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Blog blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setText(null);
                } else {
                    setText(blog.getTitle()); // Show only the title
                }
            }
        });
    }

    /**
     * Refreshes the visual cards representing posts in the UI.
     */
    /*private void refreshCards() {
        cardContainer.getChildren().clear();

        for (Post post : postList) {
            VBox card = createPostCard(post);
            cardContainer.getChildren().add(card);
        }
    }*/
    private void refreshCards() {
        // Clear the current content in the card container
        cardContainer.getChildren().clear();

        // Check if there are posts in the list
        if (postList.isEmpty()) {
            // Display a message if there are no posts
            Label noPostsLabel = new Label("No posts to display.");
            noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-font-weight: bold;");
            cardContainer.getChildren().add(noPostsLabel);
            return; // Exit the method since there are no posts to display
        }

        // Iterate through the list of posts and create cards for each
        for (Post post : postList) {
            VBox card = createPostCard(post); // Create a visual card for the post
            cardContainer.getChildren().add(card); // Add the card to the container
        }
    }

    /**
     * Creates a visual card for a given post.
     */
    private VBox createPostCard(Post post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #E8F0FE; -fx-padding: 10; -fx-border-color: #004AAD; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setPrefWidth(150);

        Text contentText = new Text("Content: " + post.getContent());
        contentText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text createdAtText = new Text("Created At: " + post.getCreatedAt());
        Text updatedAtText = new Text("Updated At: " + post.getUpdatedAt());
        Text blogText = new Text("Blog: " + (post.getBlog() != null ? post.getBlog().getTitle() : "None"));

        // Set an onMouseClicked event to populate the form when the card is clicked
        card.setOnMouseClicked(event -> {
            selectedPost = post;
            populateFields(post);
        });

        card.getChildren().addAll(contentText, createdAtText, updatedAtText, blogText);

        return card;
    }

    /**
     * Populates the form fields with the selected post's data.
     */
    private void populateFields(Post post) {
        contentField.setText(post.getContent());
        createdAtPicker.setValue(post.getCreatedAt() != null ? java.time.LocalDate.parse(post.getCreatedAt().substring(0, 10)) : null);
        updatedAtPicker.setValue(post.getUpdatedAt() != null ? java.time.LocalDate.parse(post.getUpdatedAt().substring(0, 10)) : null);
        blogComboBox.setValue(post.getBlog());

        if (post.getImage() != null) {
            postImagePreview.setImage(new Image(post.getImage())); // Display the image
            postImagePath = post.getImage(); // Store the image path
        } else {
            postImagePreview.setImage(null);
            postImagePath = null;
        }
    }

    /**
     * Handles the Add button action with validation checks.
     */
    @FXML
    private void handleAddAction(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "INSERT INTO post (content, image, created_at, update_at, blog_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, contentField.getText());
            preparedStatement.setString(2, postImagePath);
            preparedStatement.setString(3, createdAtPicker.getValue().toString());
            preparedStatement.setString(4, updatedAtPicker.getValue().toString());
            preparedStatement.setInt(5, blogComboBox.getValue().getId());
            preparedStatement.executeUpdate();

            // Retrieve the ID of the newly added post
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int postId = generatedKeys.getInt(1);

                // Add the action to the post history logs
                postHistoryLogs.add("Post added: '" + contentField.getText() + "' (ID: " + postId + ", Blog ID: " + blogComboBox.getValue().getId() + ")");
            }

            showAlert("Success", "Post added successfully!");
            fetchPostsFromDatabase();
            refreshCards();
            clearForm();

        } catch (Exception e) {
            showAlert("Error", "An error occurred while adding the post: " + e.getMessage());
        }
    }

    /**
     * Validates the form fields.
     */
    private boolean validateForm() {
        if (contentField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "The content must not be empty.");
            return false;
        }
        if (contentField.getText().length() < 10) {
            showAlert("Validation Error", "The content must have at least 10 characters.");
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
        if (blogComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select an associated blog.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        contentField.clear();
        createdAtPicker.setValue(null);
        updatedAtPicker.setValue(null);
        postImagePreview.setImage(null);
        postImagePath = null;
        selectedPost = null;
    }

   /* @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (selectedPost == null) {
            showAlert("Error", "Please select a post to update.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "UPDATE post SET content = ?, image = ?, created_at = ?, update_at = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, contentField.getText());
            preparedStatement.setString(2, postImagePath);
            preparedStatement.setString(3, createdAtPicker.getValue().toString());
            preparedStatement.setString(4, updatedAtPicker.getValue().toString());
            preparedStatement.setInt(5, selectedPost.getId());
            preparedStatement.executeUpdate();

            showAlert("Success", "Post updated successfully!");
            fetchPostsFromDatabase();
            refreshCards();
            clearForm();

        } catch (Exception e) {
            showAlert("Error", "An error occurred while updating the post: " + e.getMessage());
        }
    }*/

    //////////
    @FXML
    private void handleUpdateAction(ActionEvent event) {
        if (selectedPost == null) {
            showAlert("Error", "Please select a post to update.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            String query = "UPDATE post SET content = ?, image = ?, created_at = ?, update_at = ?, blog_id = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, contentField.getText());
            preparedStatement.setString(2, postImagePath);
            preparedStatement.setString(3, createdAtPicker.getValue().toString());
            preparedStatement.setString(4, updatedAtPicker.getValue().toString());
            preparedStatement.setInt(5, blogComboBox.getValue().getId()); // Ensure the selected blog's ID is passed
            preparedStatement.setInt(6, selectedPost.getId());
            preparedStatement.executeUpdate();

            // Add the action to the post history logs
            postHistoryLogs.add("Post updated: '" + selectedPost.getContent() + "' (ID: " + selectedPost.getId() + ", Blog ID: " + blogComboBox.getValue().getId() + ")");

            showAlert("Success", "Post updated successfully!");
            fetchPostsFromDatabase();
            refreshCards();
            clearForm();

        } catch (Exception e) {
            showAlert("Error", "An error occurred while updating the post: " + e.getMessage());
        }
    }
////


    @FXML
    private void handleDeleteAction() {
        if (selectedPost != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this post?");
            confirmationAlert.setContentText("Post Content: " + selectedPost.getContent());

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        Connection connection = MyDataBase.getInstance().getConnection();
                        String query = "DELETE FROM post WHERE id = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, selectedPost.getId());
                        preparedStatement.executeUpdate();

                        // Add the action to the post history logs
                        postHistoryLogs.add("Post deleted: '" + selectedPost.getContent() + "' (ID: " + selectedPost.getId() + ")");

                        showAlert("Success", "Post deleted successfully!");
                        fetchPostsFromDatabase();
                        refreshCards();
                        clearForm();

                    } catch (Exception e) {
                        showAlert("Error", "An error occurred while deleting the post: " + e.getMessage());
                    }
                } else {
                    showAlert("Action Canceled", "Post deletion was canceled.");
                }
            });
        } else {
            showAlert("No Selection", "Please select a post to delete.");
        }
    }
    @FXML
    private void handlePostImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Post Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(postImagePreview.getScene().getWindow());

        if (selectedFile != null) {
            try {
                postImagePath = selectedFile.toURI().toString();
                Image image = new Image(postImagePath);
                postImagePreview.setImage(image);
                showAlert("Success", "Image uploaded successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleViewStatsAction(ActionEvent event) {
        // Create a new Stage (Modal Dialog)
        Stage statsStage = new Stage();
        statsStage.initModality(Modality.APPLICATION_MODAL);
        statsStage.setTitle("General Stats Overview");

        // Create a VBox to hold the PieChart and Close Button
        VBox statsBox = new VBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-background-color: white; -fx-padding: 20;"); // White background

        // Populate the PieChart dynamically
        populateStatsPieChart(); // Ensure this method fills the statsPieChart with data

        // Add the PieChart and Close Button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #004AAD; -fx-text-fill: white; -fx-font-weight: bold;");
        closeButton.setOnAction(e -> statsStage.close());

        statsBox.getChildren().addAll(statsPieChart, closeButton);

        // Create a Scene and set it to the Stage
        Scene statsScene = new Scene(statsBox, 500, 500); // Adjust size as needed
        statsStage.setScene(statsScene);

        // Show the Modal Dialog
        statsStage.showAndWait();
    }
    /*@FXML
    private void handleViewStatsAction(ActionEvent event) {
        try {
            // Fetch stats
            int totalBlogs = blogService.getTotalBlogs();
            int totalPosts = postService.getTotalPosts();
            double averagePostsPerBlog = postService.getAveragePostsPerBlog();

            // Prepare the message
            String message = String.format(
                    "Total Blogs: %d\nTotal Posts: %d\nAverage Posts Per Blog: %.2f",
                    totalBlogs, totalPosts, averagePostsPerBlog
            );

            // Display in an alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("General Stats");
            alert.setHeaderText("Aggregated Statistics");
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Failed to fetch stats: " + e.getMessage());
        }
    }*/
    private void populateStatsPieChart() {
        try {
            // Fetch data dynamically
            int totalBlogs = blogService.getTotalBlogs();
            int totalPosts = postService.getTotalPosts();
            double averagePostsPerBlog = postService.getAveragePostsPerBlog();

            // Create data for the chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Blogs (" + totalBlogs + ")", totalBlogs),
                    new PieChart.Data("Posts (" + totalPosts + ")", totalPosts),
                    new PieChart.Data("Avg Posts/Blog (" + String.format("%.2f", averagePostsPerBlog) + ")", averagePostsPerBlog)
            );

            // Set data and customize chart
            statsPieChart.setData(pieChartData);
            statsPieChart.setLabelsVisible(true); // Show labels

            // Customize PieChart colors
            statsPieChart.getData().forEach(data -> {
                String color;
                switch (data.getName().split(" ")[0]) {
                    case "Blogs":
                        color = "#61A4BC"; // Light Blue
                        break;
                    case "Posts":
                        color = "#FFA500"; // Orange
                        break;
                    case "Avg":
                        color = "#90EE90"; // Light Green
                        break;
                    default:
                        color = "#D3D3D3"; // Default Gray
                }
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            });

        } catch (Exception e) {
            showAlert("Error", "Failed to populate stats: " + e.getMessage());
        }
    }
    /**
     * Filters posts by content based on search text (dynamic as you type)
     */
    private void filterPostsByContent(String searchText) {
        cardContainer.getChildren().clear();

        List<Post> filtered = postList.stream()
                .filter(post ->
                        searchText == null ||
                                searchText.isEmpty() ||
                                post.getContent().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        filtered.forEach(post -> {
            VBox card = createPostCard(post);
            cardContainer.getChildren().add(card);
        });
    }

    /**
     * Handles key release event for dynamic searching
     */
    @FXML
    private void handleSearchKeyReleased(KeyEvent event) {
        filterPostsByContent(searchField.getText());
    }
    @FXML
    private void handleViewHistory(ActionEvent event) {
        // Populate the history logs
        StringBuilder historyText = new StringBuilder();
        for (String log : postHistoryLogs) {
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
    private void updatePaginationControls() {
        prevPageButton.setDisable(currentPage == 1); // Disable "Previous" button if on the first page
        nextPageButton.setDisable(currentPage * postsPerPage >= totalPosts); // Disable "Next" button if on the last page
    }

   /* @FXML
    private void handlePrevPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--; // Move to the previous page
            fetchPostsFromDatabase(); // Fetch posts for the new page
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (currentPage * postsPerPage < totalPosts) {
            currentPage++; // Move to the next page
            fetchPostsFromDatabase(); // Fetch posts for the new page
        }
    }
*/
   @FXML
   private void handlePrevPage(ActionEvent event) {
       if (currentPage > 1) {
           currentPage--; // Move to the previous page
           fetchPostsFromDatabase(); // Fetch blogs for the new page
       }
   }


    @FXML
    private void handleNextPage(ActionEvent event) {
        if (currentPage * postsPerPage < totalPosts) {
            currentPage++; // Move to the next page
            fetchPostsFromDatabase();// Fetch blogs for the new page
        }
    }
    @FXML
    private void handleSortByContent(ActionEvent event) {
        // Toggle the sort order
        isContentAscending = !isContentAscending;

        // Sort the postList by content
        postList.sort((post1, post2) -> {
            if (isContentAscending) {
                return post1.getContent().compareToIgnoreCase(post2.getContent());
            } else {
                return post2.getContent().compareToIgnoreCase(post1.getContent());
            }
        });

        // Refresh the card view to display the sorted posts
        refreshCards();
    }

    private void updateSortButtonText() {
        String order = isContentAscending ? "Ascending" : "Descending";
        sortContentButton.setText("Sort by Content (" + order + ")");
    }

}