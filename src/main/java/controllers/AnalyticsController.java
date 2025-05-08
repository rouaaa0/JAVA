package controllers;

import service.BlogService;
import service.PostService;

import javafx.scene.control.Alert;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsController {

    private final BlogService blogService;
    private final PostService postService;

    public AnalyticsController() {
        // Initialize services
        this.blogService = new BlogService();
        this.postService = new PostService();
    }

    /**
     * Provides aggregated statistics for blogs and posts.
     * @return A map containing total blogs, total posts, and average posts per blog.
     */
    public Map<String, Object> getGeneralStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            int totalBlogs = blogService.getTotalBlogs();
            int totalPosts = postService.getTotalPosts();
            double averagePostsPerBlog = postService.getAveragePostsPerBlog();

            stats.put("totalBlogs", totalBlogs);
            stats.put("totalPosts", totalPosts);
            stats.put("averagePostsPerBlog", averagePostsPerBlog);

        } catch (Exception e) {
            showAlert("Error", "Failed to fetch general stats: " + e.getMessage());
        }
        return stats;
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
}