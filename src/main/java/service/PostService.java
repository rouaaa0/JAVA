package service;

import models.Post;
import utils.MyDataBase;
import utils.ProfanityFilter;
import utils.SessionManager;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostService implements IService<Post> {

    private final Connection connection;
    private final SpellCheckService spellCheckService;

    public PostService() {
        connection = MyDataBase.getInstance().getConnection();
        spellCheckService = new SpellCheckService();
    }

    // Add a new post with profanity filtering and spell checking
    @Override
    public void add(Post post) throws SQLException {
        // Log content before processing
        System.out.println("AVANT TRAITEMENT: " + post.getContent());

        // Filter content using the ProfanityFilter utility
        String filteredContent = ProfanityFilter.filterText(post.getContent());

        // Log content after filtering
        System.out.println("APRÈS FILTRAGE DE PROFANITÉ: " + filteredContent);

        // Apply spell checking if requested
        if (post.isSpellChecked()) {
            String correctedContent = spellCheckService.autoCorrectText(filteredContent);
            System.out.println("APRÈS CORRECTION ORTHOGRAPHIQUE: " + correctedContent);
            post.setContent(correctedContent);
        } else {
            post.setContent(filteredContent);
        }

        String sql = "INSERT INTO post (content, image, created_at, update_at, like_count, dislike_count, blog_id) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getImage());
            if (post.getBlog() != null) {
                ps.setInt(3, post.getBlog().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    // Select all posts
    @Override
    public List<Post> select() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, b.id as blog_id, b.title, b.description, b.created_at_blog, b.updated_at_blog " +
                "FROM post p LEFT JOIN blog b ON p.blog_id = b.id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setContent(rs.getString("content"));
                post.setImage(rs.getString("image"));
                post.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null);
                post.setUpdatedAt(rs.getTimestamp("update_at") != null ? rs.getTimestamp("update_at").toString() : null);
                post.setLikeCount(rs.getInt("like_count"));
                post.setDislikeCount(rs.getInt("dislike_count"));

                // Set blog information if available
                int blogId = rs.getInt("blog_id");
                if (!rs.wasNull()) {
                    models.Blog blog = new models.Blog();
                    blog.setId(blogId);
                    blog.setTitle(rs.getString("title"));
                    blog.setDescription(rs.getString("description"));
                    blog.setCreatedAtBlog(rs.getString("created_at_blog"));
                    blog.setUpdatedAtBlog(rs.getString("updated_at_blog"));
                    post.setBlog(blog);
                }

                // Set user reaction if user is logged in
                if (SessionManager.getCurrentUser() != null) {
                    String userReaction = getUserReactionForPost(post.getId(), SessionManager.getCurrentUser().getId());
                    post.setUserReaction(userReaction);
                }

                posts.add(post);
            }
        }
        return posts;
    }

    // Update a post with profanity filtering and spell checking
    @Override
    public void update(Post post) throws SQLException {
        // Log content before processing
        System.out.println("AVANT TRAITEMENT: " + post.getContent());

        // Filter content using the ProfanityFilter utility
        String filteredContent = ProfanityFilter.filterText(post.getContent());

        // Log content after filtering
        System.out.println("APRÈS FILTRAGE DE PROFANITÉ: " + filteredContent);

        // Apply spell checking if requested
        if (post.isSpellChecked()) {
            String correctedContent = spellCheckService.autoCorrectText(filteredContent);
            System.out.println("APRÈS CORRECTION ORTHOGRAPHIQUE: " + correctedContent);
            post.setContent(correctedContent);
        } else {
            post.setContent(filteredContent);
        }

        String sql = "UPDATE post SET content = ?, image = ?, update_at = CURRENT_TIMESTAMP, blog_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getImage());
            if (post.getBlog() != null) {
                ps.setInt(3, post.getBlog().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, post.getId());
            ps.executeUpdate();
        }
    }

    // Delete a post by ID
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM post WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Method to check spelling of a post
    public Map<String, List<String>> checkPostSpelling(Post post) {
        Map<String, List<String>> errors = spellCheckService.checkSpelling(post.getContent());
        post.setSpellErrors(errors);
        return errors;
    }

    // Method to automatically correct spelling
    public String autoCorrectPost(Post post) {
        String correctedContent = spellCheckService.autoCorrectText(post.getContent());
        return correctedContent;
    }

    // Method to count total posts
    public int getTotalPosts() throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM post";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }
        return 0;
    }

    // Method to calculate average posts per blog
    public double getAveragePostsPerBlog() throws SQLException {
        String query = "SELECT COUNT(*) * 1.0 / (SELECT COUNT(*) FROM blog) AS average FROM post";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                return resultSet.getDouble("average");
            }
        }
        return 0.0;
    }

    // Check if user has already reacted to a post
    public String getUserReactionForPost(int postId, int userId) throws SQLException {
        String query = "SELECT reaction_type FROM post_user_reaction WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("reaction_type");
                }
            }
        }
        return null; // No reaction
    }

    // Like a post with user tracking
    public boolean likePost(int postId) throws SQLException {
        // Check if user is logged in
        if (SessionManager.getCurrentUser() == null) {
            System.err.println("No user logged in");
            return false;
        }

        int userId = SessionManager.getCurrentUser().getId();
        System.out.println("User " + userId + " attempting to like post " + postId);

        // Get current counts first
        int[] currentCounts = getLikeDislikeCounts(postId);
        int currentLikes = currentCounts[0];
        int currentDislikes = currentCounts[1];
        System.out.println("Current counts - Likes: " + currentLikes + ", Dislikes: " + currentDislikes);

        // Begin transaction
        connection.setAutoCommit(false);
        try {
            // Check if the user has already reacted to this post
            String existingReaction = getUserReactionForPost(postId, userId);
            System.out.println("Existing reaction: " + existingReaction);

            boolean updateNeeded = true;

            if (existingReaction == null) {
                // No previous reaction, add a new like
                addUserReaction(postId, userId, "LIKE");
                currentLikes++;
                System.out.println("Adding new LIKE, new like count: " + currentLikes);
            } else if (existingReaction.equals("DISLIKE")) {
                // User previously disliked, change to like
                updateUserReaction(postId, userId, "LIKE");
                currentLikes++;
                currentDislikes--;
                System.out.println("Changing DISLIKE to LIKE, new counts: likes=" + currentLikes + ", dislikes=" + currentDislikes);
            } else {
                // User already liked, remove the like
                removeUserReaction(postId, userId);
                currentLikes--;
                System.out.println("Removing LIKE, new like count: " + currentLikes);
                updateNeeded = false;
            }

            // Update counts directly in database
            updatePostLikeCountDirect(postId, currentLikes, currentDislikes);

            // Commit transaction
            connection.commit();
            System.out.println("Transaction committed successfully");

            // Final check to confirm update
            int[] finalCounts = getLikeDislikeCounts(postId);
            System.out.println("Final counts after commit - Likes: " + finalCounts[0] + ", Dislikes: " + finalCounts[1]);

            return updateNeeded;
        } catch (SQLException e) {
            System.err.println("SQL ERROR during likePost: " + e.getMessage());
            e.printStackTrace();
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // Completely rewritten dislikePost method
    public boolean dislikePost(int postId) throws SQLException {
        // Check if user is logged in
        if (SessionManager.getCurrentUser() == null) {
            System.err.println("No user logged in");
            return false;
        }

        int userId = SessionManager.getCurrentUser().getId();
        System.out.println("User " + userId + " attempting to dislike post " + postId);

        // Get current counts first
        int[] currentCounts = getLikeDislikeCounts(postId);
        int currentLikes = currentCounts[0];
        int currentDislikes = currentCounts[1];
        System.out.println("Current counts - Likes: " + currentLikes + ", Dislikes: " + currentDislikes);

        // Begin transaction
        connection.setAutoCommit(false);
        try {
            // Check if the user has already reacted to this post
            String existingReaction = getUserReactionForPost(postId, userId);
            System.out.println("Existing reaction: " + existingReaction);

            boolean updateNeeded = true;

            if (existingReaction == null) {
                // No previous reaction, add a new dislike
                addUserReaction(postId, userId, "DISLIKE");
                currentDislikes++;
                System.out.println("Adding new DISLIKE, new dislike count: " + currentDislikes);
            } else if (existingReaction.equals("LIKE")) {
                // User previously liked, change to dislike
                updateUserReaction(postId, userId, "DISLIKE");
                currentLikes--;
                currentDislikes++;
                System.out.println("Changing LIKE to DISLIKE, new counts: likes=" + currentLikes + ", dislikes=" + currentDislikes);
            } else {
                // User already disliked, remove the dislike
                removeUserReaction(postId, userId);
                currentDislikes--;
                System.out.println("Removing DISLIKE, new dislike count: " + currentDislikes);
                updateNeeded = false;
            }

            // Update counts directly in database
            updatePostLikeCountDirect(postId, currentLikes, currentDislikes);

            // Commit transaction
            connection.commit();
            System.out.println("Transaction committed successfully");

            // Final check to confirm update
            int[] finalCounts = getLikeDislikeCounts(postId);
            System.out.println("Final counts after commit - Likes: " + finalCounts[0] + ", Dislikes: " + finalCounts[1]);

            return updateNeeded;
        } catch (SQLException e) {
            System.err.println("SQL ERROR during dislikePost: " + e.getMessage());
            e.printStackTrace();
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    // Add a user reaction to the post_user_reaction table
    private void addUserReaction(int postId, int userId, String reactionType) throws SQLException {
        String query = "INSERT INTO post_user_reaction (post_id, user_id, reaction_type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, reactionType);
            ps.executeUpdate();
        }
    }

    // Update a user reaction in the post_user_reaction table
    private void updateUserReaction(int postId, int userId, String newReactionType) throws SQLException {
        String query = "UPDATE post_user_reaction SET reaction_type = ? WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, newReactionType);
            ps.setInt(2, postId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    // Remove a user reaction from the post_user_reaction table
    private void removeUserReaction(int postId, int userId) throws SQLException {
        String query = "DELETE FROM post_user_reaction WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // Update post like and dislike counts
// Update post like and dislike counts - Fixed method
    private void updatePostLikeCount(int postId, int likeChange, int dislikeChange) throws SQLException {
        // Debug info before update
        int[] beforeCounts = getLikeDislikeCounts(postId);
        System.out.println("BEFORE UPDATE: Post " + postId + " has " + beforeCounts[0] +
                " likes and " + beforeCounts[1] + " dislikes");
        System.out.println("APPLYING CHANGES: like change = " + likeChange + ", dislike change = " + dislikeChange);

        // Verify the post exists
        String checkQuery = "SELECT id FROM post WHERE id = ?";
        try (PreparedStatement checkPs = connection.prepareStatement(checkQuery)) {
            checkPs.setInt(1, postId);
            ResultSet rs = checkPs.executeQuery();
            if (!rs.next()) {
                System.err.println("Error: Post with ID " + postId + " does not exist!");
                return;
            }
        }

        // Use direct value update instead of incremental update to avoid race conditions
        String query = "UPDATE post SET like_count = GREATEST(0, like_count + ?), dislike_count = GREATEST(0, dislike_count + ?) WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, likeChange);
            ps.setInt(2, dislikeChange);
            ps.setInt(3, postId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("UPDATE RESULT: " + rowsAffected + " rows affected");

            if (rowsAffected == 0) {
                System.err.println("Warning: No rows updated for post ID " + postId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating post counts: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Debug info after update
        int[] afterCounts = getLikeDislikeCounts(postId);
        System.out.println("AFTER UPDATE: Post " + postId + " now has " + afterCounts[0] +
                " likes and " + afterCounts[1] + " dislikes");
    }

    // Get like and dislike counts
    public int[] getLikeDislikeCounts(int postId) throws SQLException {
        int[] counts = new int[2]; // [likes, dislikes]
        String query = "SELECT like_count, dislike_count FROM post WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    counts[0] = rs.getInt("like_count");
                    counts[1] = rs.getInt("dislike_count");
                }
            }
        }
        return counts;
    }
    // Add this method to your PostService class
    public void verifyDatabaseSetup() throws SQLException {
        // Check if post_user_reaction table exists
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'post_user_reaction'");
            boolean tableExists = rs.next();
            System.out.println("post_user_reaction table exists: " + tableExists);

            if (!tableExists) {
                System.out.println("Creating post_user_reaction table...");
                String createTableSQL =
                        "CREATE TABLE IF NOT EXISTS post_user_reaction (" +
                                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "post_id INT NOT NULL, " +
                                "user_id INT NOT NULL, " +
                                "reaction_type ENUM('LIKE', 'DISLIKE') NOT NULL, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "UNIQUE KEY unique_user_post_reaction (post_id, user_id), " +
                                "FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE)";
                stmt.execute(createTableSQL);
                System.out.println("Table created successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error checking/creating post_user_reaction table: " + e.getMessage());
        }

        // Check post table for like_count and dislike_count columns
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "post", "like_count");
            boolean likeCountExists = columns.next();

            columns = meta.getColumns(null, null, "post", "dislike_count");
            boolean dislikeCountExists = columns.next();

            System.out.println("like_count column exists: " + likeCountExists);
            System.out.println("dislike_count column exists: " + dislikeCountExists);
        } catch (SQLException e) {
            System.err.println("Error checking columns: " + e.getMessage());
        }

        // Print current counts for debugging
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, like_count, dislike_count FROM post");
            System.out.println("\nCurrent post counts in database:");
            System.out.println("ID\tLikes\tDislikes");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" +
                        rs.getInt("like_count") + "\t" +
                        rs.getInt("dislike_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error checking post counts: " + e.getMessage());
        }
    }
    public void initializeTestCounts() throws SQLException {
        try {
            // Get all posts
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id FROM post");

            // Update each post with some random counts
            while (rs.next()) {
                int postId = rs.getInt("id");
                int likeCount = (int) (Math.random() * 10); // Random number between 0-9
                int dislikeCount = (int) (Math.random() * 5); // Random number between 0-4

                PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE post SET like_count = ?, dislike_count = ? WHERE id = ?");
                updateStmt.setInt(1, likeCount);
                updateStmt.setInt(2, dislikeCount);
                updateStmt.setInt(3, postId);
                updateStmt.executeUpdate();

                System.out.println("Updated post " + postId + " with " + likeCount +
                        " likes and " + dislikeCount + " dislikes");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing test counts: " + e.getMessage());
            throw e;
        }
    }
    private void updatePostLikeCountDirect(int postId, int newLikeCount, int newDislikeCount) throws SQLException {
        String query = "UPDATE post SET like_count = ?, dislike_count = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, Math.max(0, newLikeCount));  // Ensure counts don't go negative
            ps.setInt(2, Math.max(0, newDislikeCount));
            ps.setInt(3, postId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("DIRECT UPDATE RESULT: " + rowsAffected + " rows affected");
        }
    }

}