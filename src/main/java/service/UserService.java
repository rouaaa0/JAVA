package service;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private Connection connection;

    public UserService() {
        connection = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(User user) throws SQLException {
        String sql = "INSERT INTO user (name, lastname, email, password, role, profilepic) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            preparedStatement.setString(5, user.getRole());
            preparedStatement.setString(6, user.getProfilepic());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name=?, lastname=?, email=?, password=?, role=?, profilepic=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            preparedStatement.setString(5, user.getRole());
            preparedStatement.setString(6, user.getProfilepic());
            preparedStatement.setInt(7, user.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<User> select() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setLastname(resultSet.getString("lastname"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role"));
                user.setProfilepic(resultSet.getString("profilepic"));
                users.add(user);
            }
        }
        return users;
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                System.out.println("[DEBUG] Email found: " + email);
                System.out.println("[DEBUG] Hashed password from database: " + hashedPassword);

                if (BCrypt.checkpw(password, hashedPassword)) {
                    System.out.println("[DEBUG] Password matched successfully.");
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("lastname"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("profilepic")
                    );
                } else {
                    System.out.println("[DEBUG] Password did not match.");
                }
            } else {
                System.out.println("[DEBUG] No user found with email: " + email);
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int getRoleCount(String role) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM user WHERE role = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, role);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Fetches a subset of users based on the page size and offset for pagination.
     *
     * @param pageSize The maximum number of users to fetch (page size).
     * @param offset   The starting point for fetching users.
     * @return A list of users for the specified page.
     */
    public List<User> selectWithPagination(int pageSize, int offset) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, offset);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setLastname(resultSet.getString("lastname"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role"));
                user.setProfilepic(resultSet.getString("profilepic"));
                users.add(user);
            }
        }
        return users;
    }

    public List<User> selectWithPaginationAndSearch(int limit, int offset, String nameFilter) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?) LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + nameFilter + "%"); // Filter by name
            preparedStatement.setInt(2, limit); // Set the limit for pagination
            preparedStatement.setInt(3, offset); // Set the offset for pagination
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setLastname(resultSet.getString("lastname"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role"));
                user.setProfilepic(resultSet.getString("profilepic"));
                users.add(user);
            }
        }

        return users;
    }


    public List<User> selectWithPaginationAndSorting(int limit, int offset, String nameFilter, String sortOrder) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?) ORDER BY name " + sortOrder + " LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + nameFilter + "%"); // Filter by name
            preparedStatement.setInt(2, limit); // Set the limit for pagination
            preparedStatement.setInt(3, offset); // Set the offset for pagination
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setLastname(resultSet.getString("lastname"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role"));
                user.setProfilepic(resultSet.getString("profilepic"));
                users.add(user);
            }
        }

        return users;
    }
}