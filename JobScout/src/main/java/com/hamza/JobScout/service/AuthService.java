package com.hamza.JobScout.service;

import com.hamza.JobScout.dto.AuthResponse;
import com.hamza.JobScout.dto.LoginRequest;
import com.hamza.JobScout.dto.RegisterRequest;
import com.hamza.JobScout.dto.TokenRefreshRequest;
import com.hamza.JobScout.dto.TokenRefreshResponse;
import com.hamza.JobScout.entity.RefreshToken;
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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public void register(RegisterRequest registerRequest) {
        try {
            String sanitizedEmail = userValidator.validateAndSanitizeEmail(registerRequest.getEmail());
            if (userService.userExists(sanitizedEmail)) {
                throw new IllegalArgumentException("User with this email already exists");
            }

            String sanitizedFullName = userValidator.validateAndSanitizeFullName(registerRequest.getFullName());
            String validatedPassword = userValidator.validatePassword(registerRequest.getPassword());
            String sanitizedSerpapiKey = userValidator.validateAndSanitizeSerpapiKey(registerRequest.getSerpapiKey());
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

            // Generate access token and refresh token (this creates new session)
            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            long accessTokenExpiration = jwtService.getAccessTokenExpirationTime();

            return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                "Login successful",
                accessToken,
                refreshToken.getToken(),
                accessTokenExpiration
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            throw new RuntimeException("Login failed. Please try again.");
        }
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        try {
            String refreshTokenStr = request.getRefreshToken();
            return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(oldToken -> {
                    User user = oldToken.getUser();
                    String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
                    
                    // FIXED: Use refreshToken method that preserves session start time
                    RefreshToken newRefreshToken = refreshTokenService.refreshToken(oldToken);
                    
                    // Revoke the old refresh token
                    refreshTokenService.revokeToken(refreshTokenStr);

                    return new TokenRefreshResponse(
                        newAccessToken,
                        newRefreshToken.getToken(),
                        jwtService.getAccessTokenExpirationTime(),
                        "Token refreshed successfully"
                    );
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot refresh token: " + e.getMessage());
        }
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }
}
