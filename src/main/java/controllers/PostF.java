package controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Post;
import models.Blog;
import service.PostService;
import utils.MyDataBase;
import utils.SessionManager;

public class PostF implements Initializable {

    @FXML
    private HBox recenthb;
    @FXML
    private VBox toblogs; // Changed from toposts to toblogs to match the FXML
    @FXML
    private Connection connection;
    @FXML
    private javafx.scene.control.TextField searchField;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    @FXML
    private ToggleButton themeToggle;

    @FXML
    private Button chatButton; // Added to match the FXML

    private PostService postService;
    private boolean isDarkMode = false;
    private double xOffset = 0; // Added for chat button drag
    private double yOffset = 0; // Added for chat button drag
    private boolean wasDragged = false; // Added for chat button drag

    @FXML
    private Label postCountLabel;

    // Modify the initialize method to update the post count
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize PostService
        postService = new PostService();

        // Initialize theme toggle button
        if (themeToggle != null) {
            themeToggle.setText("Dark Mode"); // Initial text
            themeToggle.setSelected(isDarkMode);
        }
        try {
            // Add this method to check database structure
            postService.verifyDatabaseSetup();

            // Get a count of existing reactions
            String query = "SELECT COUNT(*) FROM post_user_reaction";
            Statement stmt = MyDataBase.getInstance().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("No reactions found in database, initializing test counts...");
                postService.initializeTestCounts();
            }
        } catch (SQLException e) {
            System.err.println("Error during database verification: " + e.getMessage());
            e.printStackTrace();
        }
        // Apply initial theme
        applyTheme();

        // Setup chat button (similar to BlogF)
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
            sortOrderComboBox.setOnAction(e -> sortPosts());
        }

        // Initialize search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> searchPosts(newText));
        }

        List<Post> sorted = getPostsFromDB();

        // Update post count label
        if (postCountLabel != null) {
            postCountLabel.setText(sorted.size() + " posts");
        }

        int limit = Math.min(3, sorted.size());

        // Ajouter les 3 premiers posts à la HBox
        for (int i = 0; i < limit; i++) {
            Post p = sorted.get(i);
            recenthb.getChildren().add(createPostCard(p));
        }

        // Ajouter les autres posts à la VBox (défilement)
        for (int i = 3; i < sorted.size(); i++) {
            Post p = sorted.get(i);
            toblogs.getChildren().add(createPostHBox(p)); // Changed from toposts to toblogs
        }

        // Mettre à jour l'affichage des posts
        updatePostDisplay(sorted);

        // Debug information about logged-in user
        if (SessionManager.getCurrentUser() != null) {
            System.out.println("User logged in: " + SessionManager.getCurrentUser().getName() +
                    " (ID: " + SessionManager.getCurrentUser().getId() + ")");
        } else {
            System.out.println("No user currently logged in");
        }
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
                // Update post cards and HBox items for dark mode
                updatePostItemsTheme(true);
            } else {
                // Apply Light Mode styles
                scene.getRoot().setStyle("-fx-background-color: white; -fx-text-fill: black;");
                // Update post cards and HBox items for light mode
                updatePostItemsTheme(false);
            }
        }
    }

    private void updatePostItemsTheme(boolean isDark) {
        // Update recent posts cards
        for (Node card : recenthb.getChildren()) {
            if (card instanceof AnchorPane) {
                if (isDark) {
                    card.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555;");

                    // Update labels within the card
                    for (Node child : ((AnchorPane) card).getChildren()) {
                        if (child instanceof VBox) {
                            for (Node vboxChild : ((VBox) child).getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().startsWith("Blog:") || label.getText().startsWith("Créé le:")) {
                                        label.setTextFill(Color.LIGHTGRAY);
                                    }
                                }
                                if (vboxChild instanceof HBox) {
                                    // Update buttons in likeDislikeBox if needed
                                    for (Node hboxChild : ((HBox) vboxChild).getChildren()) {
                                        if (hboxChild instanceof Button) {
                                            Button btn = (Button) hboxChild;
                                            if (btn.getText().contains("👍")) {
                                                btn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            } else if (btn.getText().contains("👎")) {
                                                btn.setStyle("-fx-background-color: #5f1e1e; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd;");

                    // Reset labels to original colors
                    for (Node child : ((AnchorPane) card).getChildren()) {
                        if (child instanceof VBox) {
                            for (Node vboxChild : ((VBox) child).getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().startsWith("Blog:")) {
                                        label.setTextFill(Color.web("#555555"));
                                    } else if (label.getText().startsWith("Créé le:")) {
                                        label.setTextFill(Color.web("#777777"));
                                    }
                                }
                                if (vboxChild instanceof HBox) {
                                    // Reset buttons in likeDislikeBox
                                    for (Node hboxChild : ((HBox) vboxChild).getChildren()) {
                                        if (hboxChild instanceof Button) {
                                            Button btn = (Button) hboxChild;
                                            if (btn.getText().contains("👍")) {
                                                btn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            } else if (btn.getText().contains("👎")) {
                                                btn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Update all posts in the VBox
        for (Node hbox : toblogs.getChildren()) { // Changed from toposts to toblogs
            if (hbox instanceof HBox) {
                if (isDark) {
                    hbox.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555555; -fx-border-radius: 5; -fx-background-radius: 5;");

                    // Update labels within the HBox
                    for (Node child : ((HBox) hbox).getChildren()) {
                        if (child instanceof VBox) {
                            for (Node vboxChild : ((VBox) child).getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().startsWith("Blog :")) {
                                        label.setTextFill(Color.LIGHTGRAY);
                                    } else if (label.getText().startsWith("Créé le :")) {
                                        label.setTextFill(Color.LIGHTGRAY);
                                    }
                                }
                                if (vboxChild instanceof HBox) {
                                    // Update buttons in likeDislikeBox
                                    for (Node hboxChild : ((HBox) vboxChild).getChildren()) {
                                        if (hboxChild instanceof Button) {
                                            Button btn = (Button) hboxChild;
                                            if (btn.getText().contains("👍")) {
                                                btn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            } else if (btn.getText().contains("👎")) {
                                                btn.setStyle("-fx-background-color: #5f1e1e; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            }
                                        }
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
                                    if (label.getText().startsWith("Blog :")) {
                                        label.setTextFill(Color.web("#555555"));
                                    } else if (label.getText().startsWith("Créé le :")) {
                                        label.setTextFill(Color.web("#777777"));
                                    }
                                }
                                if (vboxChild instanceof HBox) {
                                    // Reset buttons in likeDislikeBox
                                    for (Node hboxChild : ((HBox) vboxChild).getChildren()) {
                                        if (hboxChild instanceof Button) {
                                            Button btn = (Button) hboxChild;
                                            if (btn.getText().contains("👍")) {
                                                btn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            } else if (btn.getText().contains("👎")) {
                                                btn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updatePostDisplay(List<Post> posts) {
        toblogs.getChildren().clear(); // Changed from toposts to toblogs

        // Ajouter chaque post de la liste à la VBox
        for (Post p : posts) {
            toblogs.getChildren().add(createPostHBox(p)); // Changed from toposts to toblogs
        }

        // Update the post count label
        if (postCountLabel != null) {
            postCountLabel.setText(posts.size() + " posts");
        }
    }


    private void searchPosts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            updatePostDisplay(getPostsFromDB());
            return;
        }

        List<Post> filteredPosts = getPostsFromDB().stream()
                .filter(post ->
                        post.getContent().toLowerCase().contains(searchText.toLowerCase()) ||
                                (post.getBlog() != null && post.getBlog().getTitle().toLowerCase().contains(searchText.toLowerCase())))
                .collect(Collectors.toList());

        updatePostDisplay(filteredPosts);
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

    private void sortPosts() {
        String sortOption = sortOrderComboBox.getValue();
        List<Post> posts = getPostsFromDB();

        if (sortOption != null) {
            switch (sortOption) {
                case "Plus récents d'abord":
                    posts = posts.stream()
                            .sorted(Comparator.comparingInt(Post::getId).reversed())
                            .collect(Collectors.toList());
                    break;
                case "Plus anciens d'abord":
                    posts = posts.stream()
                            .sorted(Comparator.comparingInt(Post::getId))
                            .collect(Collectors.toList());
                    break;
                case "Alphabétique (A-Z)":
                    posts = posts.stream()
                            .sorted(Comparator.comparing(Post::getContent))
                            .collect(Collectors.toList());
                    break;
                case "Alphabétique (Z-A)":
                    posts = posts.stream()
                            .sorted(Comparator.comparing(Post::getContent).reversed())
                            .collect(Collectors.toList());
                    break;
            }
        }

        updatePostDisplay(posts);
    }

    private List<Post> getPostsFromDB() {
        ObservableList<Post> posts = FXCollections.observableArrayList();
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT p.*, b.title as blog_title, b.description as blog_description " +
                    "FROM post p " +
                    "JOIN blog b ON p.blog_id = b.id " +
                    "ORDER BY p.id DESC");
            while (rs.next()) {
                Blog blog = new Blog(
                        rs.getInt("blog_id"),
                        rs.getString("blog_title"),
                        rs.getString("blog_description"),
                        null,
                        null
                );

                Post post = new Post(
                        rs.getInt("id"),
                        rs.getString("content"),
                        rs.getString("image"),
                        rs.getString("created_at"),
                        rs.getTimestamp("update_at") != null ? String.valueOf(rs.getTimestamp("update_at").toLocalDateTime()) : null,
                        blog
                );
                post.setLikeCount(rs.getInt("like_count"));
                post.setDislikeCount(rs.getInt("dislike_count"));

                // Check if the current user has reacted to this post
                if (SessionManager.getCurrentUser() != null) {
                    try {
                        String userReaction = postService.getUserReactionForPost(post.getId(), SessionManager.getCurrentUser().getId());
                        post.setUserReaction(userReaction);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de récupération des posts");
        }

        return posts.stream()
                .sorted(Comparator.comparingInt(Post::getId).reversed())
                .collect(Collectors.toList());
    }

    private AnchorPane createPostCard(Post p) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(275.0, 140.0); // Updated size to match BlogF
        card.getStyleClass().add("post-card");
        card.setEffect(new DropShadow());
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #dddddd;");

        VBox contentBox = new VBox();
        contentBox.setSpacing(5);
        contentBox.setPadding(new Insets(10));

        Label contentLabel = new Label(truncateText(p.getContent(), 50));
        contentLabel.setFont(Font.font("Berlin Sans FB", 14.0));
        contentLabel.setTextFill(Color.web("#2262c6"));
        contentLabel.setWrapText(true);

        Label blogLabel = new Label("Blog: " + (p.getBlog() != null ? p.getBlog().getTitle() : ""));
        blogLabel.setFont(Font.font("Arial", 12.0));
        blogLabel.setTextFill(Color.web("#555555"));

        Label dateLabel = new Label("Créé le: " + p.getCreatedAt());
        dateLabel.setFont(Font.font("Arial", 12.0));
        dateLabel.setTextFill(Color.web("#777777"));

        // Add like and dislike counts
        HBox likeDislikeBox = new HBox();
        likeDislikeBox.setSpacing(10);
        likeDislikeBox.setAlignment(Pos.CENTER_LEFT);

        // Create like button with appropriate styling based on user's reaction
        Button likeBtn = new Button("👍 " + p.getLikeCount());

        // Create dislike button with appropriate styling based on user's reaction
        Button dislikeBtn = new Button("👎 " + p.getDislikeCount());

        // Set initial styling based on user reactions
        if (p.hasUserLiked()) {
            likeBtn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
            dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else if (p.hasUserDisliked()) {
            dislikeBtn.setStyle("-fx-background-color: #5f1e1e; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
            likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
            dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
        }

        likeBtn.setOnAction(e -> {
            if (SessionManager.getCurrentUser() == null) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez vous connecter pour liker ce post.");
                return;
            }

            try {
                System.out.println("Attempting to like post ID: " + p.getId() +
                        " by user ID: " + SessionManager.getCurrentUser().getId());
                System.out.println("Current like count: " + p.getLikeCount() + ", dislike count: " + p.getDislikeCount());

                boolean liked = postService.likePost(p.getId());

                // Refresh the post data to get updated counts
                int[] counts = postService.getLikeDislikeCounts(p.getId());
                System.out.println("After like operation - Database counts: Likes=" + counts[0] + ", Dislikes=" + counts[1]);

                p.setLikeCount(counts[0]);
                p.setDislikeCount(counts[1]);

                // Check user's new reaction
                String reaction = postService.getUserReactionForPost(p.getId(), SessionManager.getCurrentUser().getId());
                System.out.println("User reaction after like: " + reaction);
                p.setUserReaction(reaction);

                // Update button appearance
                if ("LIKE".equals(reaction)) {
                    likeBtn.setStyle("-fx-background-color: #1e3a5f !important; -fx-text-fill: white !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                    dislikeBtn.setStyle("-fx-background-color: #ffebee !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                } else {
                    likeBtn.setStyle("-fx-background-color: #e1f5fe !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                }

                // Update button text
                likeBtn.setText("👍 " + p.getLikeCount());
                dislikeBtn.setText("👎 " + p.getDislikeCount());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du like: " + ex.getMessage());
            }
        });

        dislikeBtn.setOnAction(e -> {
            if (SessionManager.getCurrentUser() == null) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez vous connecter pour disliker ce post.");
                return;
            }

            try {
                System.out.println("Attempting to dislike post ID: " + p.getId() +
                        " by user ID: " + SessionManager.getCurrentUser().getId());
                System.out.println("Current like count: " + p.getLikeCount() + ", dislike count: " + p.getDislikeCount());

                boolean disliked = postService.dislikePost(p.getId());

                // Refresh the post data to get updated counts
                int[] counts = postService.getLikeDislikeCounts(p.getId());
                System.out.println("After dislike operation - Database counts: Likes=" + counts[0] + ", Dislikes=" + counts[1]);

                p.setLikeCount(counts[0]);
                p.setDislikeCount(counts[1]);

                // Check user's new reaction
                String reaction = postService.getUserReactionForPost(p.getId(), SessionManager.getCurrentUser().getId());
                System.out.println("User reaction after dislike: " + reaction);
                p.setUserReaction(reaction);

                // Update button appearance
                if ("DISLIKE".equals(reaction)) {
                    dislikeBtn.setStyle("-fx-background-color: #5f1e1e !important; -fx-text-fill: white !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                    likeBtn.setStyle("-fx-background-color: #e1f5fe !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                } else {
                    dislikeBtn.setStyle("-fx-background-color: #ffebee !important; -fx-border-radius: 5 !important; -fx-background-radius: 5 !important;");
                }

                // Update button text
                likeBtn.setText("👍 " + p.getLikeCount());
                dislikeBtn.setText("👎 " + p.getDislikeCount());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du dislike: " + ex.getMessage());
            }
        });
        likeDislikeBox.getChildren().addAll(likeBtn, dislikeBtn);

        // If post has an image, display it
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(p.getImage()));
                imageView.setFitWidth(255.0);
                imageView.setFitHeight(70.0);
                imageView.setPreserveRatio(true);
                contentBox.getChildren().add(imageView);
            } catch (Exception e) {
                // If image cannot be loaded, just skip it
                System.out.println("Cannot load image: " + p.getImage());
            }
        }

        contentBox.getChildren().addAll(contentLabel, blogLabel, dateLabel, likeDislikeBox);

        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);
        AnchorPane.setBottomAnchor(contentBox, 0.0);

        card.getChildren().add(contentBox);

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
    }    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    // Added setupChatButton method from BlogF controller
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
            if (!wasDragged && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                handleOpenChat(new ActionEvent(chatButton, null));
            }
            event.consume();
        });
    }

    // Added snapToEdge method from BlogF controller
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
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();

        // Animation principale
        timeline.getKeyFrames().addAll(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                        new javafx.animation.KeyValue(chatButton.layoutXProperty(), targetX, javafx.animation.Interpolator.EASE_OUT),
                        new javafx.animation.KeyValue(chatButton.layoutYProperty(), targetY, javafx.animation.Interpolator.EASE_OUT))
        );

        // Premier rebond
        timeline.getKeyFrames().addAll(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300),
                        new javafx.animation.KeyValue(chatButton.layoutXProperty(), snapToRight ? targetX - 25 : targetX + 25, javafx.animation.Interpolator.EASE_OUT),
                        new javafx.animation.KeyValue(chatButton.layoutYProperty(), targetY + 15, javafx.animation.Interpolator.EASE_OUT))
        );

        // Position finale
        timeline.getKeyFrames().addAll(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(500),
                        new javafx.animation.KeyValue(chatButton.layoutXProperty(), targetX, javafx.animation.Interpolator.EASE_OUT),
                        new javafx.animation.KeyValue(chatButton.layoutYProperty(), targetY, javafx.animation.Interpolator.EASE_OUT))
        );

        timeline.play();
    }

    @FXML
    private void handleOpenChat(ActionEvent event) {
        // Vérifier si le chat est déjà ouvert
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
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
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

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
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(400), root);
            scaleIn.setFromX(0.3);
            scaleIn.setFromY(0.3);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(fadeIn, scaleIn);
            parallelTransition.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
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
    private void goToPostPage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/PostPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBlogsPage(ActionEvent event) {
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des utilisateurs.");
        }
    }

    @FXML
    private void navigateToProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profileDialog.fxml"));
            Parent root = loader.load();
            Stage profileStage = new Stage();
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.initStyle(StageStyle.DECORATED);
            profileStage.setTitle("Modifier le profil");
            profileStage.setScene(new Scene(root));
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setX(primaryStage.getX() + 100);
            profileStage.setY(primaryStage.getY() + 100);
            profileStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de profil.");
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        SessionManager.logout();
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

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private HBox createPostHBox(Post post) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(15.0);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        // If post has an image, display it
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            try {
                ImageView imageView = new ImageView(new Image(post.getImage()));
                imageView.setFitWidth(80.0);
                imageView.setFitHeight(80.0);
                imageView.setPreserveRatio(true);
                hbox.getChildren().add(imageView);
            } catch (Exception e) {
                // If image cannot be loaded, just use a placeholder
                System.out.println("Cannot load image: " + post.getImage());
            }
        }

        Label contentLabel = new Label("Contenu : " + truncateText(post.getContent(), 100));
        contentLabel.setTextFill(Color.web("#2262c6"));
        contentLabel.setFont(Font.font("Berlin Sans FB", 14.0));
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(400);

        Label blogLabel = new Label("Blog : " + (post.getBlog() != null ? post.getBlog().getTitle() : ""));
        blogLabel.setTextFill(Color.web("#555555"));
        blogLabel.setFont(Font.font("Arial", 13.0));

        Label dateLabel = new Label("Créé le : " + post.getCreatedAt());
        dateLabel.setTextFill(Color.web("#777777"));
        dateLabel.setFont(Font.font("Arial", 12.0));

        // Add like and dislike buttons with counts
        HBox likeDislikeBox = new HBox();
        likeDislikeBox.setSpacing(10);
        likeDislikeBox.setAlignment(Pos.CENTER_LEFT);

        // Create like button with appropriate styling based on user's reaction
        Button likeBtn = new Button("👍 " + post.getLikeCount());

        // Create dislike button with appropriate styling based on user's reaction
        Button dislikeBtn = new Button("👎 " + post.getDislikeCount());

        // Set initial styling based on user reactions
        if (post.hasUserLiked()) {
            likeBtn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
            dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else if (post.hasUserDisliked()) {
            dislikeBtn.setStyle("-fx-background-color: #5f1e1e; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
            likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
            dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
        }

        likeBtn.setOnAction(e -> {
            if (SessionManager.getCurrentUser() == null) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez vous connecter pour liker ce post.");
                return;
            }

            try {
                boolean liked = postService.likePost(post.getId());

                // Refresh the post data to get updated counts
                int[] counts = postService.getLikeDislikeCounts(post.getId());
                post.setLikeCount(counts[0]);
                post.setDislikeCount(counts[1]);

                // Check user's new reaction
                String reaction = postService.getUserReactionForPost(post.getId(), SessionManager.getCurrentUser().getId());
                post.setUserReaction(reaction);

                // Update button appearance
                if ("LIKE".equals(reaction)) {
                    likeBtn.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                    dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
                } else {
                    likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
                }

                // Update button text
                likeBtn.setText("👍 " + post.getLikeCount());
                dislikeBtn.setText("👎 " + post.getDislikeCount());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du like");
            }
        });

        dislikeBtn.setOnAction(e -> {
            if (SessionManager.getCurrentUser() == null) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Veuillez vous connecter pour disliker ce post.");
                return;
            }

            try {
                boolean disliked = postService.dislikePost(post.getId());

                // Refresh the post data to get updated counts
                int[] counts = postService.getLikeDislikeCounts(post.getId());
                post.setLikeCount(counts[0]);
                post.setDislikeCount(counts[1]);

                // Check user's new reaction
                String reaction = postService.getUserReactionForPost(post.getId(), SessionManager.getCurrentUser().getId());
                post.setUserReaction(reaction);

                // Update button appearance
                if ("DISLIKE".equals(reaction)) {
                    dislikeBtn.setStyle("-fx-background-color: #5f1e1e; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
                    likeBtn.setStyle("-fx-background-color: #e1f5fe; -fx-border-radius: 5; -fx-background-radius: 5;");
                } else {
                    dislikeBtn.setStyle("-fx-background-color: #ffebee; -fx-border-radius: 5; -fx-background-radius: 5;");
                }

                // Update button text
                likeBtn.setText("👍 " + post.getLikeCount());
                dislikeBtn.setText("👎 " + post.getDislikeCount());

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du dislike");
            }
        });

        likeDislikeBox.getChildren().addAll(likeBtn, dislikeBtn);

        VBox textBox = new VBox(contentLabel, blogLabel, dateLabel, likeDislikeBox);
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
                hbox.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });

        return hbox;
    }

}