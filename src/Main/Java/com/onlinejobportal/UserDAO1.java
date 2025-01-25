package com.onlinejobportal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO1 {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO1.class);

    private boolean isValidUser(User user) {
        return user != null &&
                user.getUsername() != null &&
                user.getPassword() != null &&
                user.getEmail() != null &&
                user.getRole() != null;
    }

    public boolean addUser(User user) {
        if (!isValidUser(user)) {
            logger.warn("Invalid user data provided");
            return false;
        }

        String query = "INSERT INTO Users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, PasswordUtil.hashPassword(user.getPassword())); // Hash the password
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            logger.error("Error adding user to the database", e);
        }
        return false;
    }

    public User getUserById(int userId) {
        String query = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                );
            } else {
                logger.info("No user found with ID: {}", userId);
            }

        } catch (SQLException e) {
            logger.error("Error fetching user by ID", e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM Users";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }

    public List<User> getAllUsers(int limit, int offset) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM Users LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            logger.error("Error fetching users with pagination", e);
        }
        return users;
    }

    public boolean updateUser(User user) {
        if (!isValidUser(user) || user.getUserId() <= 0) {
            logger.info("Invalid user data provided for update: {}", user);
            return false;
        }

        String query = "UPDATE Users SET username = ?, password = ?, email = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, PasswordUtil.hashPassword(user.getPassword())); // Hash the password
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            logger.error("Error updating user in database", e);
        }
        return false;
    }

    public boolean isEmailRegistered(String email) {
        String query = "SELECT COUNT(*) FROM Users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if count > 0
            }
        } catch (SQLException e) {
            logger.error("Error checking if email is registered", e);
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            logger.warn("Invalid user ID provided for deletion");
            return false;
        }

        String query = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            logger.error("Error deleting user from database", e);
        }
        return false;
    }
}