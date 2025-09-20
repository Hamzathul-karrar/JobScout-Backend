package com.hamza.JobScout.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean revoked = false;

    // NEW: Track the original session start time for absolute timeout
    @Column(nullable = false)
    private LocalDateTime sessionStartTime;

    // NEW: Track when this specific token was issued
    @Column(nullable = false)
    private LocalDateTime tokenIssuedAt;

    // Constructors
    public RefreshToken() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.tokenIssuedAt = now;
    }

    public RefreshToken(String token, LocalDateTime expiryDate, User user) {
        this();
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    public RefreshToken(String token, LocalDateTime expiryDate, User user, LocalDateTime sessionStartTime) {
        this();
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
        this.sessionStartTime = sessionStartTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(LocalDateTime sessionStartTime) { this.sessionStartTime = sessionStartTime; }

    public LocalDateTime getTokenIssuedAt() { return tokenIssuedAt; }
    public void setTokenIssuedAt(LocalDateTime tokenIssuedAt) { this.tokenIssuedAt = tokenIssuedAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    // NEW: Check if the entire session has exceeded maximum duration
    public boolean isSessionExpired(long maxSessionDurationSeconds) {
        if (sessionStartTime == null) {
            return false; // Backward compatibility
        }
        return LocalDateTime.now().isAfter(sessionStartTime.plusSeconds(maxSessionDurationSeconds));
    }
}
