package com.hamza.JobScout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hamza.JobScout.service.AuthService;
import com.hamza.JobScout.dto.RegisterRequest;
import com.hamza.JobScout.dto.LoginRequest;
import com.hamza.JobScout.dto.ApiResponse;
import com.hamza.JobScout.dto.AuthResponse;
import com.hamza.JobScout.dto.TokenRefreshRequest;
import com.hamza.JobScout.dto.TokenRefreshResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok(new ApiResponse("User registered successfully!", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("Registration failed: " + e.getMessage(), false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, null, e.getMessage(), null, null, 0));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            TokenRefreshResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new TokenRefreshResponse(null, null, 0, "Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody(required = false) TokenRefreshRequest request) {
        try {
            String refreshToken = request != null ? request.getRefreshToken() : null;
            authService.logout(refreshToken);
            return ResponseEntity.ok(new ApiResponse("Logged out successfully!", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("Logged out successfully!", true)); // Always succeed logout
        }
    }
}
