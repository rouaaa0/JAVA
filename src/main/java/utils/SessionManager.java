package utils;

import models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionManager {
    private static LocalDateTime lastLoginTime;
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        // Update last login time when setting a new user
        lastLoginTime = LocalDateTime.now();
    }

    public static int getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getId();
        }
        return -1; // Return -1 to indicate no user is logged in
    }

    public static void clearSession() {
        currentUser = null;
        // Reset last login time
        lastLoginTime = null;
    }

    public static void logout() {
        currentUser = null;
        // Add any other cleanup tasks here if needed
        // For example: clearing tokens, resetting session variables, etc.
    }

    // Get the last login time as a formatted string
    public static String getLastLoginTime() {
        if (lastLoginTime == null) {
            return "Non disponible"; // If no login time is recorded
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastLoginTime.format(formatter); // Format the date and time
    }

    // Set last login time manually (useful for loading from persistent storage)
    public static void setLastLoginTime(LocalDateTime loginTime) {
        lastLoginTime = loginTime;
    }
}