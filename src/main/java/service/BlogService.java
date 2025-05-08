package service;

import models.Blog;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*public class BlogService implements IService<Blog> {
    Connection connection;

    public BlogService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Blog blog) throws SQLException {
        String sql = "INSERT INTO blog (title, description, created_at_blog, updated_at_blog) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, blog.getTitle());
        ps.setString(2, blog.getDescription());

        // Convert String to Timestamp
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Created at
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Updated at
        ps.executeUpdate();
    }

    @Override
    public void update(Blog blog) throws SQLException {
        String sql = "UPDATE blog SET title = ?, description = ?, updated_at_blog = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, blog.getTitle());
        ps.setString(2, blog.getDescription());

        // Convert String to Timestamp
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Updated at
        ps.setInt(4, blog.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM blog WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Blog> select() throws SQLException {
        List<Blog> blogs = new ArrayList<>();
        String sql = "SELECT * FROM blog";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setTitle(rs.getString("title"));
            blog.setDescription(rs.getString("description"));

            // Convert Timestamp to String
            Timestamp createdAt = rs.getTimestamp("created_at_blog");
            if (createdAt != null) {
                blog.setCreatedAtBlog(createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at_blog");
            if (updatedAt != null) {
                blog.setUpdatedAtBlog(updatedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            blogs.add(blog);
        }

        return blogs;
    }

    public Blog getById(int id) throws SQLException {
        String sql = "SELECT * FROM blog WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setTitle(rs.getString("title"));
            blog.setDescription(rs.getString("description"));

            // Convert Timestamp to String
            Timestamp createdAt = rs.getTimestamp("created_at_blog");
            if (createdAt != null) {
                blog.setCreatedAtBlog(createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at_blog");
            if (updatedAt != null) {
                blog.setUpdatedAtBlog(updatedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            return blog;
        }

        return null;
    }
    // Add these methods to BlogService.java
    public int getTotalBlogs() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM blog";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }*/



public class BlogService implements IService<Blog> {
    Connection connection;

    public BlogService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(Blog blog) throws SQLException {
        String sql = "INSERT INTO blog (title, description, created_at_blog, updated_at_blog) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, blog.getTitle());
        ps.setString(2, blog.getDescription());

        // Convert String to Timestamp
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Created at
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Updated at
        ps.executeUpdate();
    }

    @Override
    public void update(Blog blog) throws SQLException {
        String sql = "UPDATE blog SET title = ?, description = ?, updated_at_blog = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, blog.getTitle());
        ps.setString(2, blog.getDescription());

        // Convert String to Timestamp
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Updated at
        ps.setInt(4, blog.getId());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM blog WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Blog> select() throws SQLException {
        List<Blog> blogs = new ArrayList<>();
        String sql = "SELECT * FROM blog";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setTitle(rs.getString("title"));
            blog.setDescription(rs.getString("description"));

            // Convert Timestamp to String
            Timestamp createdAt = rs.getTimestamp("created_at_blog");
            if (createdAt != null) {
                blog.setCreatedAtBlog(createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at_blog");
            if (updatedAt != null) {
                blog.setUpdatedAtBlog(updatedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            blogs.add(blog);
        }

        return blogs;
    }

    public Blog getById(int id) throws SQLException {
        String sql = "SELECT * FROM blog WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Blog blog = new Blog();
            blog.setId(rs.getInt("id"));
            blog.setTitle(rs.getString("title"));
            blog.setDescription(rs.getString("description"));

            // Convert Timestamp to String
            Timestamp createdAt = rs.getTimestamp("created_at_blog");
            if (createdAt != null) {
                blog.setCreatedAtBlog(createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at_blog");
            if (updatedAt != null) {
                blog.setUpdatedAtBlog(updatedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            return blog;
        }

        return null;
    }
    // Add these methods to BlogService.java
    public int getTotalBlogs() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM blog";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }
}

