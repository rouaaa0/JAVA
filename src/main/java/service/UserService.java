package service;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private final Connection connection;

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
            preparedStatement.setString(4, hashPassword(user.getPassword())); // Hash the password before saving
            preparedStatement.setString(5, user.getRole());
            preparedStatement.setString(6, user.getProfilepic());
            preparedStatement.executeUpdate();
        }
    }

    /* public void add(User user) throws SQLException {
         String sql = "INSERT INTO user (name, lastname, email, password, role, profilepic) VALUES (?, ?, ?, ?, ?, ?)";
         try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
             preparedStatement.setString(1, user.getName());
             preparedStatement.setString(2, user.getLastname());
             preparedStatement.setString(3, user.getEmail());
             preparedStatement.setString(4, user.getPassword()); // Store plain text password
             preparedStatement.setString(5, user.getRole());
             preparedStatement.setString(6, user.getProfilepic());
             preparedStatement.executeUpdate();
         }
     }*/
    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name=?, lastname=?, email=?, password=?, role=?, profilepic=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, hashPassword(user.getPassword())); // Hash the password before updating
            preparedStatement.setString(5, user.getRole());
            preparedStatement.setString(6, user.getProfilepic());
            preparedStatement.setInt(7, user.getId());
            preparedStatement.executeUpdate();
        }
    }

    /*public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name=?, lastname=?, email=?, password=?, role=?, profilepic=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getPassword()); // Store plain text password
            preparedStatement.setString(5, user.getRole());
            preparedStatement.setString(6, user.getProfilepic());
            preparedStatement.setInt(7, user.getId());
            preparedStatement.executeUpdate();
        }
    }
*/
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
                users.add(mapResultSetToUser(resultSet));
            }
        }
        return users;
    }

/*
  public User login(String email, String plainPassword) throws SQLException {
       String sql = "SELECT * FROM user WHERE email = ?";
       try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
           preparedStatement.setString(1, email);
           ResultSet resultSet = preparedStatement.executeQuery();

           if (resultSet.next()) {
               String storedPassword = resultSet.getString("password");
               boolean passwordMatch = false;

               // Try BCrypt verification first
               try {
                   if (BCrypt.checkpw(plainPassword, storedPassword)) {
                       passwordMatch = true;
                   }
               } catch (IllegalArgumentException e) {
                   // If BCrypt verification fails due to format, check if it's a plain text or other hash
                   if (plainPassword.equals(storedPassword)) {
                       passwordMatch = true;
                       // Optionally update to BCrypt here
                   }
               }

               if (passwordMatch) {
                   return mapResultSetToUser(resultSet);
               }
           }
       }
       return null;
  }
*/

    public User login(String email, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        System.out.println("Attempting login for email: " + email);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                System.out.println("Found user with stored password hash: " + storedPassword);
                System.out.println("Attempting to verify password...");

                boolean passwordMatch = false;

                // Try BCrypt verification
                try {
                    System.out.println("Comparing with BCrypt...");
                    passwordMatch = BCrypt.checkpw(plainPassword, storedPassword);
                    System.out.println("BCrypt comparison result: " + passwordMatch);
                } catch (IllegalArgumentException e) {
                    System.out.println("BCrypt validation failed with exception: " + e.getMessage());
                    // If BCrypt verification fails due to format, check if it's a plain text
                    System.out.println("Falling back to plain comparison...");
                    if (plainPassword.equals(storedPassword)) {
                        passwordMatch = true;
                        System.out.println("Plain text comparison matched");
                        // Optionally update to BCrypt here
                    }
                }

                if (passwordMatch) {
                    System.out.println("Password verified successfully");
                    return mapResultSetToUser(resultSet);
                } else {
                    System.out.println("Password verification failed");
                }
            } else {
                System.out.println("No user found with email: " + email);
            }
        }
        return null;
    }

    // Add this test method somewhere
    private void testBCrypt() {
        String testPassword = "myTestPassword";
        String hashed = BCrypt.hashpw(testPassword, BCrypt.gensalt());
        System.out.println("Test hash: " + hashed);
        System.out.println("Verification result: " + BCrypt.checkpw(testPassword, hashed));
        System.out.println("Verification with wrong password: " + BCrypt.checkpw("wrongPassword", hashed));
    }


    // For password creation/updating
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    // For verification
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }


    public void updatePasswordToBCrypt(int userId, String plainPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, hashPassword(plainPassword));
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        }
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

    public List<User> selectWithPagination(int pageSize, int offset) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, offset);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        return users;
    }

    public List<User> selectWithPaginationAndSearch(int limit, int offset, String nameFilter) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?) LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + nameFilter + "%");
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, offset);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        return users;
    }

    public List<User> selectWithPaginationAndSorting(int limit, int offset, String nameFilter, String sortOrder) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE LOWER(name) LIKE LOWER(?) ORDER BY name " + sortOrder + " LIMIT ? OFFSET ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + nameFilter + "%");
            preparedStatement.setInt(2, limit);
            preparedStatement.setInt(3, offset);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setLastname(resultSet.getString("lastname"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password")); // Note: Password is stored as hashed
        user.setRole(resultSet.getString("role"));
        user.setProfilepic(resultSet.getString("profilepic"));
        return user;
    }
/*
    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainPassword The plain-text password to hash.
     * @return The hashed password.

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }*/


    // Add a method in UserService to update a password by email
    public boolean resetPassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, hashPassword(newPassword));
            preparedStatement.setString(2, email);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }


    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The User object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        }
        return null;
    }
}