package service;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    Connection connection;

    public UserService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String sql = "INSERT INTO user (name, lastname, email, password, role, profilepic) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getLastname());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        preparedStatement.setString(5, user.getRole());
        preparedStatement.setString(6, user.getProfilepic());
        preparedStatement.executeUpdate();
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name=?, lastname=?, email=?, password=?, role=?, profilepic=? WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getLastname());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        preparedStatement.setString(5, user.getRole());
        preparedStatement.setString(6, user.getProfilepic());
        preparedStatement.setInt(7, user.getId());
        preparedStatement.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<User> select() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

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

        return users;
    }

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            String hashedPassword = rs.getString("password");
            if (BCrypt.checkpw(password, hashedPassword)) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("profilepic")
                );
            }
        }

        return null;
    }

    public boolean modifierMotDePasse(int userId, String ancien, String nouveau) throws SQLException {
        String sqlSelect = "SELECT password FROM user WHERE id = ?";
        PreparedStatement selectStmt = connection.prepareStatement(sqlSelect);
        selectStmt.setInt(1, userId);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            String hashed = rs.getString("password");
            if (BCrypt.checkpw(ancien, hashed)) {
                String nouveauHashed = BCrypt.hashpw(nouveau, BCrypt.gensalt());
                String sqlUpdate = "UPDATE user SET password = ? WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate);
                updateStmt.setString(1, nouveauHashed);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                return true;
            }
        }

        return false;
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
}
