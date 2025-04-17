package service;

import models.Post;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService {

    private final Connection connection;

    public PostService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    // Add a new post
    public void add(Post post) throws SQLException {
        String sql = "INSERT INTO post (content, image, created_at, update_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getImage());
            ps.executeUpdate();
        }
    }

    // Select all posts
    public List<Post> select() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setContent(rs.getString("content"));
                post.setImage(rs.getString("image"));
                post.setCreatedAt(rs.getTimestamp("created_at").toString());
                posts.add(post);
            }
        }
        return posts;
    }

    // Update a post
    public void update(Post post) throws SQLException {
        String sql = "UPDATE post SET content = ?, image = ?, update_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setString(2, post.getImage());
            ps.setInt(3, post.getId());
            ps.executeUpdate();
        }
    }

    // Delete a post by ID
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM post WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id); // Set the ID of the post to delete
            ps.executeUpdate();
        }
    }
}