package utils;

import java.security.SecureRandom;

/**
 * Utility class for generating secure random passwords
 */
public class PasswordGenerator {
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random password based on specified parameters
     *
     * @param length The length of the password
     * @param includeLowercase Include lowercase letters
     * @param includeUppercase Include uppercase letters
     * @param includeNumbers Include numeric characters
     * @param includeSpecial Include special characters
     * @return A randomly generated password
     */
    public static String generatePassword(int length, boolean includeLowercase, boolean includeUppercase,
                                          boolean includeNumbers, boolean includeSpecial) {
        // Ensure at least one character set is selected
        if (!(includeLowercase || includeUppercase || includeNumbers || includeSpecial)) {
            includeLowercase = true; // Default to at least lowercase if nothing selected
        }

        // Build the character pool
        StringBuilder charPool = new StringBuilder();

        if (includeLowercase) charPool.append(LOWERCASE_CHARS);
        if (includeUppercase) charPool.append(UPPERCASE_CHARS);
        if (includeNumbers) charPool.append(NUMBERS);
        if (includeSpecial) charPool.append(SPECIAL_CHARS);

        String pool = charPool.toString();
        int poolSize = pool.length();

        // Generate the password
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(poolSize);
            password.append(pool.charAt(randomIndex));
        }

        // Ensure password contains at least one character from each selected type
        if (length >= 4) {
            ensurePasswordStrength(password, includeLowercase, includeUppercase, includeNumbers, includeSpecial);
        }

        return password.toString();
    }

    /**
     * Ensures the password has at least one character from each selected type
     */
    private static void ensurePasswordStrength(StringBuilder password, boolean includeLowercase, boolean includeUppercase,
                                               boolean includeNumbers, boolean includeSpecial) {
        int position = 0;

        if (includeLowercase) {
            position = random.nextInt(password.length());
            password.setCharAt(position, LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        }

        if (includeUppercase) {
            do {
                position = random.nextInt(password.length());
            } while (includeLowercase && Character.isLowerCase(password.charAt(position)));
            password.setCharAt(position, UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        }

        if (includeNumbers) {
            do {
                position = random.nextInt(password.length());
            } while ((includeLowercase && Character.isLowerCase(password.charAt(position))) ||
                    (includeUppercase && Character.isUpperCase(password.charAt(position))));
            password.setCharAt(position, NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }

        if (includeSpecial) {
            do {
                position = random.nextInt(password.length());
            } while ((includeLowercase && Character.isLowerCase(password.charAt(position))) ||
                    (includeUppercase && Character.isUpperCase(password.charAt(position))) ||
                    (includeNumbers && Character.isDigit(password.charAt(position))));
            password.setCharAt(position, SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        }
    }

    /**
     * Generates a strong password with default settings
     *
     * @return A strong random password
     */
    public static String generateStrongPassword() {
        // Default to 12 characters with all character types
        return generatePassword(12, true, true, true, true);
    }

    /**
     * Estimates the strength of a password
     *
     * @param password The password to evaluate
     * @return A strength rating from 0-100
     */
    public static int calculatePasswordStrength(String password) {
        int score = 0;

        // Base score based on length
        if (password.length() >= 8) score += 10;
        if (password.length() >= 10) score += 10;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 14) score += 10;

        // Check for character types
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        // Add points for character variety
        if (hasLower) score += 15;
        if (hasUpper) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        return Math.min(100, score);
    }
}