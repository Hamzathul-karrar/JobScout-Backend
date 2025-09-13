package com.hamza.JobScout.dto;

public class AuthResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String message;
    private String token;
    private long expiresIn;

    public AuthResponse(Long userId, String fullName, String email, String message, String token, long expiresIn) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.message = message;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    // Existing getters and setters...
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    // New getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
