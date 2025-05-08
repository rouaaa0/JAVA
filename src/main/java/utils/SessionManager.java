package utils;

import models.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class SessionManager {
    private static LocalDateTime lastLoginTime;
    private static User currentUser;
    private static final String SESSION_FILE = "session.properties";

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        // Update last login time when setting a new user
        lastLoginTime = LocalDateTime.now();
        // Save session to file
        saveSession();
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
        // Delete session file
        deleteSessionFile();
    }

    public static void logout() {
        currentUser = null;
        // Delete session file
        deleteSessionFile();
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

    // Save session to file
    private static void saveSession() {
        if (currentUser == null) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("userId", String.valueOf(currentUser.getId()));
        properties.setProperty("email", currentUser.getEmail());
        properties.setProperty("password", currentUser.getPassword()); // Note: This should be the hashed password
        properties.setProperty("lastLoginTime", LocalDateTime.now().toString());

        try (FileOutputStream fos = new FileOutputStream(SESSION_FILE)) {
            properties.store(fos, "User Session");
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    // Load session from file
    public static boolean loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (!sessionFile.exists()) {
            return false;
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(SESSION_FILE)) {
            properties.load(fis);

            int userId = Integer.parseInt(properties.getProperty("userId", "-1"));
            String email = properties.getProperty("email", "");
            String password = properties.getProperty("password", "");

            if (userId != -1 && !email.isEmpty() && !password.isEmpty()) {
                // Create a temporary user with the saved credentials
                // Note: We'll verify this with the database in the Home class
                User user = new User();
                user.setId(userId);
                user.setEmail(email);
                user.setPassword(password);

                String lastLoginTimeStr = properties.getProperty("lastLoginTime", "");
                if (!lastLoginTimeStr.isEmpty()) {
                    try {
                        lastLoginTime = LocalDateTime.parse(lastLoginTimeStr);
                    } catch (Exception e) {
                        lastLoginTime = LocalDateTime.now();
                    }
                }

                currentUser = user;
                return true;
            }
        } catch (IOException e) {
            System.err.println("Failed to load session: " + e.getMessage());
        }

        return false;
    }

    // Delete session file
    private static void deleteSessionFile() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
    }
}