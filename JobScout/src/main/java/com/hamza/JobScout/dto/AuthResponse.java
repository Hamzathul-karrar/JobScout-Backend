package com.hamza.JobScout.dto;

public class AuthResponse {
    private Long userId;
    private String fullName;
    private String email;
    private Integer apiCallCount;
    private String message;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public AuthResponse(Long userId, String fullName, String email, Integer apiCallCount, String message, 
                       String accessToken, String refreshToken, long expiresIn) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.apiCallCount = apiCallCount;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

	public Integer getApiCallCount() {
		return apiCallCount;
	}

	public void setApiCallCount(Integer apiCallCount) {
		this.apiCallCount = apiCallCount;
	}
}
