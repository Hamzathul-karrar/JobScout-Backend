// TokenRefreshResponse.java
package com.hamza.JobScout.dto;

public class TokenRefreshResponse {
    
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String message;
    
    public TokenRefreshResponse(String accessToken, String refreshToken, long expiresIn, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.message = message;
    }
    
    // Getters and setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
