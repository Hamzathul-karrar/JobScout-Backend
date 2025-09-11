package com.hamza.JobScout.service;

import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    /**
     * Validate and sanitize email
     */
    public String validateAndSanitizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        String sanitized = email.toLowerCase().trim();
        if (!isValidEmail(sanitized)) {
            throw new IllegalArgumentException("Please provide a valid email address");
        }
        return sanitized;
    }

    /**
     * Validate and sanitize full name
     */
    public String validateAndSanitizeFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        String sanitized = fullName.trim();
        if (sanitized.length() < 2 || sanitized.length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters");
        }
        // Remove extra whitespaces between words
        sanitized = sanitized.replaceAll("\\s+", " ");
        return sanitized;
    }

    /**
     * Validate password
     */
    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters and contain letters and numbers");
        }
        return password;
    }

    /**
     * Validate and sanitize SerpApi key
     */
    public String validateAndSanitizeSerpapiKey(String serpapiKey) {
        if (serpapiKey == null || serpapiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("SerpApi key is required");
        }
        String sanitized = serpapiKey.trim();
        
        // Basic validation for SerpApi key format (they are typically alphanumeric)
        if (sanitized.length() < 10 || sanitized.length() > 100) {
            throw new IllegalArgumentException("SerpApi key appears to be invalid (length should be between 10-100 characters)");
        }
        
        // Check if it contains only valid characters (letters, numbers, underscores, hyphens)
        if (!sanitized.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("SerpApi key contains invalid characters");
        }
        
        return sanitized;
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Basic email validation
        return email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 && 
               email.length() < 255 &&
               !email.startsWith("@") && 
               !email.endsWith("@") &&
               !email.startsWith(".") &&
               !email.endsWith(".") &&
               email.indexOf("@") != email.lastIndexOf("@") == false && // Only one @
               email.indexOf("@") < email.lastIndexOf("."); // @ comes before last .
    }

    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasLetter && hasDigit;
    }

    /**
     * Validate login email (simpler validation for login)
     */
    public String validateLoginEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.toLowerCase().trim();
    }

    /**
     * Validate login password (basic validation for login)
     */
    public String validateLoginPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        return password;
    }

}

