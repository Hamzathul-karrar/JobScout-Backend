package com.hamza.JobScout.service;

import com.hamza.JobScout.dto.AuthResponse;
import com.hamza.JobScout.dto.LoginRequest;
import com.hamza.JobScout.dto.RegisterRequest;
import com.hamza.JobScout.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserValidator userValidator;

    public void register(RegisterRequest registerRequest) {
        try {
        	// Validate and sanitize inputs - capture the sanitized values
        	String sanitizedEmail = userValidator.validateAndSanitizeEmail(registerRequest.getEmail());

        	// Check if user already exists using sanitized email
            if (userService.userExists(sanitizedEmail)) {
            	throw new IllegalArgumentException("User with this email already exists");
            }
            
            String sanitizedFullName = userValidator.validateAndSanitizeFullName(registerRequest.getFullName());
            String validatedPassword = userValidator.validatePassword(registerRequest.getPassword());
            String sanitizedSerpapiKey = userValidator.validateAndSanitizeSerpapiKey(registerRequest.getSerpapiKey());


            // Create user with sanitized values
            String encodedPassword = passwordService.encodePassword(validatedPassword);
            userService.createUser(sanitizedFullName, sanitizedEmail, encodedPassword, sanitizedSerpapiKey);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            throw new RuntimeException("Registration failed. Please check your input and try again.");
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Validate and sanitize login inputs
            String sanitizedEmail = userValidator.validateLoginEmail(loginRequest.getEmail());
            String validatedPassword = userValidator.validateLoginPassword(loginRequest.getPassword());

            Optional<User> userOptional = userService.findByEmail(sanitizedEmail);
            if (userOptional.isEmpty()) {
                throw new IllegalArgumentException("Invalid email or password");
            }

            User user = userOptional.get();
            if (!passwordService.matches(validatedPassword, user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }

            userService.updateLastLogin(user);
            return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), "Login successful");

        } catch (IllegalArgumentException e) {
            throw e; // Don't expose validation details
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            throw new RuntimeException("Login failed. Please try again.");
        }
    }
}
