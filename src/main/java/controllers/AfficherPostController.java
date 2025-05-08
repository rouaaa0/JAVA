package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import java.util.Map;
import service.BlogService;
import service.PostService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import java.util.stream.Collectors;
import java.io.*;
import models.SentimentResult;
import service.SentimentService;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.concurrent.Task;
import java.util.Map;

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

    // Added UI elements for spell checking
    @FXML
    private CheckBox spellCheckEnabledCheckBox; // To enable/disable spell checking
    @FXML
    private Button checkSpellingButton; // Button to check spelling manually
    @FXML
    private TextArea spellErrorsArea; // To display spelling errors

    private List<Post> postList = new ArrayList<>();
    private List<Blog> blogList = new ArrayList<>(); // To hold blogs for the ComboBox
    private Post selectedPost = null; // To keep track of the selected post
    private String postImagePath = null; // To store the path of the uploaded image
    private final BlogService blogService = new BlogService();
    private final PostService postService = new PostService();
    private  SentimentService sentimentService = new SentimentService();

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
    @FXML
    private Button analyzeSentimentButton;
    @FXML
    private Label sentimentResultLabel;
    @FXML
    private ProgressBar sentimentScoreBar;
    @FXML
    private HBox sentimentContainer;

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

    // Update this method to include sentiment data in the card
    private VBox createPostCard(Post post) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #E8F0FE; -fx-padding: 10; -fx-border-color: #004AAD; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setPrefWidth(150);

        Text contentText = new Text("Content: " + post.getContent());
        contentText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text createdAtText = new Text("Created At: " + post.getCreatedAt());
        Text updatedAtText = new Text("Updated At: " + post.getUpdatedAt());
        Text blogText = new Text("Blog: " + (post.getBlog() != null ? post.getBlog().getTitle() : "None"));

        // Add sentiment info if available
        VBox sentimentInfo = new VBox(2);
        if (post.getSentimentLabel() != null) {
            Text sentimentText = new Text("Sentiment: " + post.getSentimentLabel());
            // Set text color based on sentiment
            if ("Positive".equals(post.getSentimentLabel())) {
                sentimentText.setFill(Color.GREEN);
            } else if ("Negative".equals(post.getSentimentLabel())) {
                sentimentText.setFill(Color.RED);
            } else {
                sentimentText.setFill(Color.GRAY);
            }
            sentimentInfo.getChildren().add(sentimentText);
        }

        // Set an onMouseClicked event to populate the form when the card is clicked
        card.setOnMouseClicked(event -> {
            selectedPost = post;
            populateFields(post);
        });

        card.getChildren().addAll(contentText, createdAtText, updatedAtText, blogText, sentimentInfo);

        return card;
    }

    /**
     * Populates the form fields with the selected post's data.
     */
    // Update the populateFields method to display sentiment data
    private void populateFields(Post post) {
        contentField.setText(post.getContent());
        createdAtPicker.setValue(post.getCreatedAt() != null ? java.time.LocalDate.parse(post.getCreatedAt().substring(0, 10)) : null);
        updatedAtPicker.setValue(post.getUpdatedAt() != null ? java.time.LocalDate.parse(post.getUpdatedAt().substring(0, 10)) : null);
        blogComboBox.setValue(post.getBlog());

        // Set spell check checkbox based on post's spell check status if available
        spellCheckEnabledCheckBox.setSelected(post.isSpellChecked());

        // Display sentiment data if available
        if (post.getSentimentLabel() != null) {
            sentimentResultLabel.setText(post.getSentimentLabel());

            // Set color based on sentiment
            switch (post.getSentimentLabel()) {
                case "Positive":
                    sentimentResultLabel.setTextFill(Color.GREEN);
                    break;
                case "Negative":
                    sentimentResultLabel.setTextFill(Color.RED);
                    break;
                default: // Neutral
                    sentimentResultLabel.setTextFill(Color.GRAY);
                    break;
            }

            // Update progress bar with sentiment score
            double normalizedScore = (post.getSentimentScore() + 1) / 2; // Convert from [-1,1] to [0,1]
            sentimentScoreBar.setProgress(normalizedScore);

            // Show sentiment container
            sentimentContainer.setVisible(true);
        } else {
            // Reset sentiment UI if no data available
            sentimentResultLabel.setText("");
            sentimentScoreBar.setProgress(0.5); // Neutral position
            sentimentContainer.setVisible(false);
        }

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
            // Create a Post object
            Post post = new Post();
            post.setContent(contentField.getText());
            post.setImage(postImagePath);
            post.setBlog(blogComboBox.getValue());

            // Set spell checking flag based on checkbox
            post.setSpellChecked(spellCheckEnabledCheckBox.isSelected());

            // Add sentiment data if available
            if (sentimentResultLabel.getText() != null &&
                    !sentimentResultLabel.getText().isEmpty() &&
                    !sentimentResultLabel.getText().equals("Analyzing...")) {
                post.setSentimentLabel(sentimentResultLabel.getText());
                // Calculate score from progress bar
                double normalizedScore = sentimentScoreBar.getProgress();
                double score = (normalizedScore * 2) - 1; // Convert back from [0,1] to [-1,1]
                post.setSentimentScore(score);
            }

            // Use the PostService to add the post (which will apply profanity filtering)
            postService.add(post);

            // Add the action to the post history logs
            String sentimentInfo = "";
            if (post.getSentimentLabel() != null) {
                sentimentInfo = ", Sentiment: " + post.getSentimentLabel();
            }

            postHistoryLogs.add("Post added: '" + post.getContent() + "' (Blog: " +
                    blogComboBox.getValue().getTitle() + ", Spell checked: " +
                    (post.isSpellChecked() ? "Yes" : "No") + sentimentInfo + ")");

            showAlert("Success", "Post added successfully" +
                    (post.isSpellChecked() ? " (with spell check and profanity filter)" : " (with profanity filter)") + "!");

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

    // Update this method to include sentiment info
    private void clearForm() {
        contentField.clear();
        createdAtPicker.setValue(null);
        updatedAtPicker.setValue(null);
        postImagePreview.setImage(null);
        postImagePath = null;
        selectedPost = null;
        spellCheckEnabledCheckBox.setSelected(false);
        spellErrorsArea.clear();

        // Reset sentiment UI
        sentimentResultLabel.setText("");
        sentimentScoreBar.setProgress(0.5); // Neutral position
        sentimentContainer.setVisible(false);
    }

    /**
     * Handles the Check Spelling button action.
     */
    @FXML
    private void handleCheckSpelling(ActionEvent event) {
        if (contentField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter content to check spelling.");
            return;
        }

        // Create a temporary post with the content
        Post tempPost = new Post();
        tempPost.setContent(contentField.getText());

        // Check spelling using the PostService
        Map<String, List<String>> spellingErrors = postService.checkPostSpelling(tempPost);

        if (spellingErrors.isEmpty()) {
            spellErrorsArea.setText("No spelling errors found.");
        } else {
            // Format and display the spelling errors
            StringBuilder errorText = new StringBuilder("Spelling errors found:\n\n");
            for (Map.Entry<String, List<String>> entry : spellingErrors.entrySet()) {
                errorText.append("• \"").append(entry.getKey()).append("\" - Suggestions: ");
                errorText.append(String.join(", ", entry.getValue())).append("\n");
            }
            spellErrorsArea.setText(errorText.toString());
        }
    }

    /**
     * Handles the Auto-Correct button action.
     */
    @FXML
    private void handleAutoCorrect(ActionEvent event) {
        if (contentField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter content to auto-correct.");
            return;
        }

        // Create a temporary post with the content
        Post tempPost = new Post();
        tempPost.setContent(contentField.getText());

        // Auto-correct the content
        String correctedContent = postService.autoCorrectPost(tempPost);

        // Update the content field with the corrected text
        contentField.setText(correctedContent);

        showAlert("Success", "Text has been auto-corrected.");
    }

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
            // Update the selectedPost object with form values
            selectedPost.setContent(contentField.getText());
            selectedPost.setImage(postImagePath);
            selectedPost.setBlog(blogComboBox.getValue());

            // Set spell checking flag based on checkbox
            selectedPost.setSpellChecked(spellCheckEnabledCheckBox.isSelected());

            // Use the PostService to update the post (which will apply profanity filtering)
            postService.update(selectedPost);

            // Add the action to the post history logs
            postHistoryLogs.add("Post updated: '" + selectedPost.getContent() +
                    "', Blog: " + blogComboBox.getValue().getTitle() +
                    ", Spell checked: " + (selectedPost.isSpellChecked() ? "Yes" : "No") + ")");


            showAlert("Success", "Post updated successfully" +
                    (selectedPost.isSpellChecked() ? " (with spell check and profanity filter)" : " (with profanity filter)") + "!");

            fetchPostsFromDatabase();
            refreshCards();
            clearForm();
        } catch (Exception e) {
            showAlert("Error", "An error occurred while updating the post: " + e.getMessage());
        }
    }

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
                        postHistoryLogs.add("Post deleted: '" + selectedPost.getContent() + "'");

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
    // Add this method to handle sentiment analysis
    @FXML
    private void handleAnalyzeSentiment(ActionEvent event) {
        if (contentField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter content to analyze sentiment.");
            return;
        }

        try {
            // Show analyzing status
            sentimentResultLabel.setText("Analyzing...");
            sentimentResultLabel.setTextFill(Color.GRAY);

            // Create a background task for the API call
            Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() throws Exception {
                    return sentimentService.analyzeSentiment(contentField.getText());
                }
            };

            // Handle the result when the task completes
            task.setOnSucceeded(e -> {
                Map<String, Object> result = task.getValue();
                double score = (double) result.get("score");
                String sentiment = (String) result.get("sentiment");

                // Update the UI with the result
                updateSentimentUI(sentiment, score);

                // If a post is selected, update its sentiment info
                if (selectedPost != null) {
                    selectedPost.setSentimentScore(score);
                    selectedPost.setSentimentLabel(sentiment);
                }
            });

            task.setOnFailed(e -> {
                showAlert("Error", "Failed to analyze sentiment: " + task.getException().getMessage());
                sentimentResultLabel.setText("Analysis failed");
                sentimentResultLabel.setTextFill(Color.RED);
            });

            // Start the background task
            new Thread(task).start();
        } catch (Exception e) {
            showAlert("Error", "An error occurred while analyzing sentiment: " + e.getMessage());
        }
    }
    // Add this method to update the sentiment UI
    private void updateSentimentUI(String sentiment, double score) {
        // Update the label with the sentiment result
        sentimentResultLabel.setText(sentiment);

        // Set color based on sentiment
        switch (sentiment) {
            case "Positive":
                sentimentResultLabel.setTextFill(Color.GREEN);
                break;
            case "Negative":
                sentimentResultLabel.setTextFill(Color.RED);
                break;
            default: // Neutral
                sentimentResultLabel.setTextFill(Color.GRAY);
                break;
        }
        // Update the progress bar
        // Convert score from [-1,1] to [0,1] for the progress bar
        double normalizedScore = (score + 1) / 2;
        sentimentScoreBar.setProgress(normalizedScore);

        // Customize progress bar color based on sentiment
        String barStyleClass = "sentiment-neutral";
        if (sentiment.equals("Positive")) {
            barStyleClass = "sentiment-positive";
        } else if (sentiment.equals("Negative")) {
            barStyleClass = "sentiment-negative";
        }

        // Reset all style classes and add the appropriate one
        sentimentScoreBar.getStyleClass().removeAll("sentiment-positive", "sentiment-negative", "sentiment-neutral");
        sentimentScoreBar.getStyleClass().add(barStyleClass);

        // Show the sentiment container
        sentimentContainer.setVisible(true);
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
            showAlert("Error", "Failed to navigate to Blog page: " + e.getMessage());
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
            showAlert("Error", "Failed to navigate to Post page: " + e.getMessage());
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
            showAlert("Error", "Failed to navigate to Club page: " + e.getMessage());
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
            showAlert("Error", "Failed to navigate to Event page: " + e.getMessage());
        }
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
            showAlert("Error", "Failed to navigate to User management page: " + e.getMessage());
        }
    }

}